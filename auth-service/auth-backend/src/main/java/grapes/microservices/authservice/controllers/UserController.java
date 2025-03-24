package grapes.microservices.authservice.controllers;

import grapes.microservices.authservice.dto.EmailDTO;
import grapes.microservices.authservice.dto.UserDTO;
import grapes.microservices.authservice.mapper.UserMapper;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import grapes.microservices.authservice.utils.AuthLogger;

/**
 * UserController handles HTTP requests related to user management.
 * It provides endpoints for user registration, retrieval, update, and deletion.
 * Endpoints:
 * - POST /auth/users/register → Register a new user.
 * - DELETE /auth/users/delete/{id} → Delete a user by ID.
 * - PUT /auth/users/update → Update an existing user.
 * - POST /auth/users/email → Retrieve a user by email.
 * - GET /auth/users/{id} → Retrieve a user by ID.
 * @author  Cameron
 */
@RestController
@RequestMapping("/auth/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final UserMapper userMapper;

    private static final Logger logger = AuthLogger.getLogger();


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        logger.info("Received request to register a user: {}", userDTO);
        try {
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        logger.info("Received request to delete user with ID: {}", id);
        try {
            userService.deleteUser(id);
            logger.info("User successfully deleted with ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Error during user deletion: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO userDTO) {
        logger.info("Received request to update user: {}", userDTO);
        try {
            User updatedUser = userService.editUser(userMapper.toEntity(userDTO));
            logger.info("User successfully updated: {}", updatedUser);
            return ResponseEntity.ok(userMapper.toDTO(updatedUser));
        } catch (IllegalArgumentException e) {
            logger.warn("Error during user update: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/email")
    public ResponseEntity<?> getUserByEmail(@RequestBody EmailDTO emailDTO) {
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
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
}

