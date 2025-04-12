package grapes.microservices.authservice.controllers;

import grapes.microservices.authservice.dto.EmailDTO;
import grapes.microservices.authservice.dto.UserDTO;
import grapes.microservices.authservice.mapper.UserMapper;
import grapes.microservices.authservice.models.AmountRequest;
import grapes.microservices.authservice.models.PasswordRequest;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.TokenService;
import grapes.microservices.authservice.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import grapes.microservices.authservice.utils.AuthLogger;

/**
 * UserController handles HTTP requests related to user management.
 * It provides endpoints for user registration, retrieval, update, and deletion.
 * @author  Cameron
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private final TokenService tokenService;

    @Autowired
    private final AuthController authController;

    private static final Logger logger = LoggerFactory.getLogger(AuthLogger.class);


    @Transactional
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        logger.info("Received request to register a user: {}", userDTO);
        try {
            userDTO.setActive(true);
            userDTO.setLoyaltyPoints(0);
            User savedUser = userService.registerUser(userMapper.toEntity(userDTO));
            logger.info("User successfully registered: {}", savedUser);
            return ResponseEntity.ok(userMapper.toDTO(savedUser));
        } catch (IllegalArgumentException e) {
            logger.warn("Error during user registration: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unknown error during user registration", e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping(value = "/disable/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> disable(@PathVariable String id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(!authController.isValidSession(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        logger.info("Received request to disable user with ID: {}", id);
        try {
            User disableUser = userService.disableUser(id);
            logger.info("User successfully disabled with ID: {}", id);
            return ResponseEntity.ok(userMapper.toDTO(disableUser));
        } catch (IllegalArgumentException e) {
            logger.warn("Error during user deactivation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Transactional
    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping(value = "/enable/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enable(@PathVariable String id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(!authController.isValidSession(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        logger.info("Received request to enable user with ID: {}", id);
        try {
            User enabledUser = userService.enableUser(id);
            logger.info("User successfully enabled with ID: {}", id);
            return ResponseEntity.ok(userMapper.toDTO(enabledUser));
        } catch (IllegalArgumentException e) {
            logger.warn("Error during user activation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Transactional
    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping(value = "/update/password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updatePassword(@RequestBody PasswordRequest password, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(!authController.isValidSession(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        logger.info("Received request to update password");
        try {
            if (password.getPassword() == null) {
                logger.warn("Password is null in the request body");
                return ResponseEntity.badRequest().body("Password cannot be null");
            }
            String idStr = tokenService.extractUserId(token.substring(7));
            User updatedUser = userService.updatePassword(userService.getUserById(idStr), password.getPassword());
            logger.info("User password successfully updated: {}", updatedUser);
            return ResponseEntity.ok(userMapper.toDTO(updatedUser));
        } catch (IllegalArgumentException e) {
            logger.warn("Error during password update: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping(value = "/update/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUser(@RequestBody UserDTO userDTO, @PathVariable String id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(!authController.isValidSession(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        logger.info("Received request to update user: {}", id);
        try {
            if (userDTO.getPassword() == null) {
                logger.warn("Password is null in the request body");
                return ResponseEntity.badRequest().body("Password cannot be null");
            }
            User userToUpdate = userMapper.toEntity(userDTO);
            if (userToUpdate.getId() != null && !userToUpdate.getId().toHexString().equals(id)) {
                logger.error("User ID in request body does not match path variable: {}", id);
                return ResponseEntity.badRequest().body("User ID in request body does not match path variable");
            }

            User updatedUser = userService.editUser(id, userToUpdate);

            if (!updatedUser.getId().toHexString().equals(id)) {
                logger.error("Updated user ID does not match the requested ID: {}", id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("The updated user does not match the requested ID.");
            }
            logger.info("User successfully updated: {}", updatedUser);
            return ResponseEntity.ok(userMapper.toDTO(updatedUser));
        } catch (IllegalArgumentException e) {
            logger.warn("Error during user update: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserByEmail(@RequestBody EmailDTO emailDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(!authController.isValidSession(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        logger.info("Received request to get user by email: {}", emailDTO.getEmail());
        try {
            User user = userService.getUserByEmail(emailDTO.getEmail());
            logger.info("User found by email: {}", user);
            return ResponseEntity.ok(userMapper.toDTO(user));
        } catch (IllegalArgumentException e) {
            logger.warn("Error during fetching user by email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserById(@PathVariable String id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(!authController.isValidSession(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        logger.info("Received request to get user by ID: {}", id);
        try {
            User user = userService.getUserById(id);
            logger.info("User found by ID: {}", user);
            return ResponseEntity.ok(userMapper.toDTO(user));
        } catch (IllegalArgumentException e) {
            logger.warn("Error during fetching user by ID: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Transactional
    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping(value = "/{id}/points/add/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addPoints(@PathVariable String id, @RequestBody AmountRequest amount, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(!authController.isValidSession(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        logger.info("Received request to add {} points to user by ID: {}", amount, id);
        try {
            userService.addLoyaltyPoints(id, amount.getAmount());
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            logger.warn("Error during adding point(s) to user : {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Transactional
    @CrossOrigin(origins = "http://localhost:3000")
    @PutMapping(value = "/{id}/points/remove/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> removePoints(@PathVariable String id, @RequestBody AmountRequest amount, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(!authController.isValidSession(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        logger.info("Received request to remove {} points to user by ID: {}", amount, id);
        try {
            userService.deductLoyaltyPoints(id, amount.getAmount());
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            logger.warn("Error during adding point(s) to user : {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/points")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> getPoints(@PathVariable String id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if(!authController.isValidSession(token)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        logger.info("Received request to get points from user by ID: {}", id);
        try {
            int userPoints = userService.getLoyaltyPoints(id);
            return ResponseEntity.ok(userPoints);
        } catch (Exception e) {
            logger.warn("Error during fetching user's point : {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

