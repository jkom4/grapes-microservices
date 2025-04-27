package grapes.microservices.authservice.controllers;

import grapes.microservices.authservice.dto.*;
import grapes.microservices.authservice.dto.UserDTOWithPasswordAndPin;
import grapes.microservices.authservice.mapper.UserMapper;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.EIDCardService;
import grapes.microservices.authservice.services.TokenService;
import grapes.microservices.authservice.services.UserService;
import grapes.microservices.authservice.services.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController handles HTTP requests related to user management.
 * It provides endpoints for user registration, retrieval, update, and deletion.
 * @author  Cameron
 */
@CrossOrigin
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private final TokenService tokenService;

    @Autowired
    private final EIDCardService eidCardService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        try {
            String token = authService.checkUserIsAuthenticated(request);
            List<User> users = userService.getAllUsers(token);
            List<UserDTOWithoutPasswordAndPin> usersDto = userMapper.toDTOList(users);
            return ResponseEntity.ok(usersDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserById(@PathVariable String id, HttpServletRequest request) {
        try {
            authService.checkUserIsAuthenticated(request);
            User user = userService.getUserById(id, true);
            UserDTOWithoutPasswordAndPin userDTO = userMapper.toDTO(user);
            return ResponseEntity.ok(userDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserByEmail(@RequestBody EmailDTO emailDTO, HttpServletRequest request) {
        try {
            authService.checkUserIsAuthenticated(request);
            User user = userService.getUserByEmail(emailDTO.getEmail());
            return ResponseEntity.ok(userMapper.toDTO(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new JsonMessage(e.getMessage()));
        }
    }

    @Transactional
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTOWithPasswordAndPin userDTO) {
        try {
            User userToRegister = userMapper.toEntity(userDTO);
            User savedUser = userService.registerUser(userToRegister);
            userService.sendRegistrationToQueue(savedUser.getId().toHexString(), savedUser.getEmail(), savedUser.getName(), savedUser.getFirstName(), savedUser.getGender(), savedUser.getBirthDate(), savedUser.getNationalId(), savedUser.getDeliveryAddress());
            return ResponseEntity.ok(userMapper.toDTO(savedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new JsonMessage(e.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDTOWithoutPasswordAndPin userDTO, @PathVariable String id, HttpServletRequest request) {
        try {
            authService.checkUserIsAuthenticated(request);

            User userToUpdate = userMapper.toEntity(userDTO);
            User updatedUser = userService.updateUser(id, userToUpdate);

            return ResponseEntity.ok(userMapper.toDTO(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new JsonMessage(e.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @PutMapping(value = "/{id}/password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePassword(@RequestBody PasswordRequest passwordReq, HttpServletRequest request) {
        try {
            String token = authService.checkUserIsAuthenticated(request);

            String idStr = tokenService.extractUserId(token);
            User userToUpdate = userService.getUserById(idStr, false);
            User updatedUser = userService.editPassword(userToUpdate, passwordReq.getCurrentPassword(), passwordReq.getUpdatedPassword());

            return ResponseEntity.ok(userMapper.toDTO(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new JsonMessage(e.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @PutMapping(value = "/{id}/pin", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePin(@RequestBody PinRequest pinReq, HttpServletRequest request) {
        try {
            String token = authService.checkUserIsAuthenticated(request);

            String idStr = tokenService.extractUserId(token);
            User userToUpdate = userService.getUserById(idStr, false);

            userToUpdate.setUpdatedAt(new java.util.Date());
            User updatedUser = userService.editPin(userToUpdate, pinReq.getCurrentPin(), pinReq.getUpdatedPin());
            return ResponseEntity.ok(userMapper.toDTO(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new JsonMessage(e.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @PutMapping(value = "/disable/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> disable(@PathVariable String id, HttpServletRequest request) {
        try {
            authService.checkUserIsAuthenticated(request);
            User disableUser = userService.disableUser(id);
            return ResponseEntity.ok(userMapper.toDTO(disableUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new JsonMessage(e.getMessage()));
        }
    }

    @Transactional
    @PutMapping(value = "/enable/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enable(@PathVariable String id, HttpServletRequest request) {
        try {
            authService.checkUserIsAuthenticated(request);
            User enabledUser = userService.enableUser(id);
            return ResponseEntity.ok(userMapper.toDTO(enabledUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Transactional
    @PutMapping(value = "/{id}/points/add/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addPoints(@PathVariable String id, @RequestBody AmountRequest amount, HttpServletRequest request) {
        try {
            authService.checkUserIsAuthenticated(request);
            userService.addLoyaltyPoints(id, amount.getAmount());
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Transactional
    @PutMapping(value = "/{id}/points/remove/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> removePoints(@PathVariable String id, @RequestBody AmountRequest amount, HttpServletRequest request) {
        try {
            authService.checkUserIsAuthenticated(request);
            userService.deductLoyaltyPoints(id, amount.getAmount());
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(value = "/{id}/points", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPoints(@PathVariable String id, HttpServletRequest request) {
        try {
            authService.checkUserIsAuthenticated(request);
            int userPoints = userService.getLoyaltyPoints(id);
            return ResponseEntity.ok(userPoints);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register/eid")
    public ResponseEntity<?> registerWithEID(@Valid @RequestBody EIDRegisterDTO additionalInfo) {
        try {
            User savedUser = eidCardService.registerWithEIDCard(additionalInfo);
            userService.sendRegistrationToQueue(savedUser.getId().toHexString(), savedUser.getEmail(), savedUser.getName(), savedUser.getFirstName(), savedUser.getGender(), savedUser.getBirthDate(), savedUser.getNationalId(), savedUser.getDeliveryAddress());
            return ResponseEntity.ok(userMapper.toDTO(savedUser));
        } catch (Exception e) {
            String message = e.getMessage();
            if (message.contains("eID middleware not found")) {
                return ResponseEntity.status(500).body(new JsonMessage("eID error: Missing middleware. Please install from https://eid.belgium.be/en/download/15/license"));
            } else if (message.contains("No card detected")) {
                return ResponseEntity.status(400).body(new JsonMessage("eID error: No card detected in reader"));
            }
            return ResponseEntity.status(500).body(new JsonMessage("eID (register) Error : " + e.getMessage()));
        }
    }
}