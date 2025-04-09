package grapes.microservices.authservice.services;

import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.repositories.UserRepository;
import grapes.microservices.authservice.utils.AuthLogger;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static grapes.microservices.authservice.validators.UserValidator.isValid;

/**
 * UserService provides CRUD operations for managing User entities.
 * This service handles user registration, retrieval, update, and deletion.
 * It ensures password encryption, email uniqueness, and user validation before processing.
 * @author Cameron
 */
@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthLogger.class);

    /**
     * Register a new user and verify the password strength and email uniqueness
     * Hash the password before saving it with bcrypt
     *
     * @param user the user to register
     * @return the registered user
     */
    @Transactional
    public User registerUser(User user) throws Exception {
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.error("Registration failed: an account already exists with this email: {}", user.getEmail());
            throw new IllegalArgumentException("Already exists an account with this email");
        }
        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            logger.error("Registration failed: an account already exists with this phone number: {}", user.getPhoneNumber());
            throw new IllegalArgumentException("Already exists an account with this phone number");
        }
        if(isValid(user)) {
            user.encryptUser();
            logger.info("User registered successfully with email: {}", user.getEmail());
            return userRepository.save(user);
        }
        logger.error("Registration failed: user is not valid for email: {}", user.getEmail());
        throw new IllegalArgumentException("User is not valid");
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param idStr The ID of the user to retrieve.
     * @return The user associated with the provided ID.
     * @throws IllegalArgumentException if no user is found with the provided ID.
     */
    public User getUserById(String idStr) throws IllegalArgumentException {
        if (idStr == null) {
            logger.error("User retrieval failed: ID cannot be null");
            throw new IllegalArgumentException("ID cannot be null");
        }
        ObjectId id = new ObjectId(idStr);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("No user found with ID: {}", idStr);
                    return new IllegalArgumentException("No user found with this ID");
                });
    }

    /**
     * Retrieves a user by their email.
     *
     * @param email The email of the user to retrieve.
     * @return The user associated with the provided email.
     * @throws IllegalArgumentException if no user is found with the provided email.
     */
    public User getUserByEmail(String email) throws IllegalArgumentException {
        if (email == null) {
            logger.error("User retrieval failed: Email cannot be null");
            throw new IllegalArgumentException("Email cannot be null");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("No user found with email: {}", email);
                    return new IllegalArgumentException("No user found with this email");
                });
    }

    /**
     * Edits the information of an existing user.
     *
     * @param updatedUser The updated user information.
     * @return The updated user.
     * @throws IllegalArgumentException if no user is found with the provided ID.
     */
    @Transactional
    public User editUser(String idStr, User updatedUser) throws Exception {
        User user = getUserById(idStr);
        if (!user.verifyPassword(updatedUser.getPassword())) {
            logger.warn("Password verification failed for user: {}", idStr);
            throw new IllegalArgumentException("Password verification failed");
        }
        user.update(updatedUser);
        if(isValid(user)) {
            user.encryptUser();
            logger.info("User with ID: {} updated successfully", user.getId());
            return userRepository.save(user);
        }
        logger.error("User update failed: User with ID: {} is not valid", user.getId());
        throw new IllegalArgumentException("User is not valid");
    }

    /**
     * Disable a user by their ID.
     *
     * @param idStr The ID of the user to delete.
     * @throws IllegalArgumentException if no user is found with the provided ID.
     */
    @Transactional
    public User disableUser(String idStr) throws IllegalArgumentException {
        if (idStr == null) {
            logger.error("Deactivation failed: ID cannot be null");
            throw new IllegalArgumentException("ID cannot be null");
        }
        User user = getUserById(idStr);
        if (!user.isActive()) {
            throw new IllegalArgumentException("Deactivation failed : this user is already disabled");
        }
        // Set user status to false
        user.setActive(false);
        logger.info("User with ID: {} disabled successfully", idStr);
        return userRepository.save(user);
    }

    /**
     * Enable (reactivate) a user by their ID.
     *
     * @param idStr The ID of the user to reactivate.
     * @throws IllegalArgumentException if no user is found with the provided ID.
     */
    @Transactional
    public User enableUser(String idStr) throws IllegalArgumentException {
        if (idStr == null) {
            logger.error("Reactivation failed: ID cannot be null");
            throw new IllegalArgumentException("ID cannot be null");
        }
        User user = getUserById(idStr);
        if (user.isActive()) {
            throw new IllegalArgumentException("Activation failed : this user is already enabled");
        }
        // Set user status to true
        user.setActive(true);
        logger.info("User with ID: {} enabled successfully", idStr);
        return userRepository.save(user);
    }

    /**
     * Adds loyalty points to a user by their ID.
     *
     * @param idStr  the ID of the user
     * @param points the number of points to add
     */
    @Transactional
    public void addLoyaltyPoints(String idStr, int points) {
        if (idStr == null) {
            logger.error("Loyalty points update failed: ID cannot be null");
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (points < 0) {
            logger.error("Loyalty points update failed: Points to add cannot be negative");
            throw new IllegalArgumentException("Points to add cannot be negative");
        }
        if (points == 0 ) {
            logger.error("Loyalty points update failed: Points to add cannot be O");
            throw new IllegalArgumentException("Points to add cannot be 0");
        }
        ObjectId id = new ObjectId(idStr);
        userRepository.updateLoyaltyPoints(id, points);
    }

    /**
     * Deducts loyalty points from a user by their ID.
     *
     * @param idStr  the ID of the user
     * @param points the number of points to deduct
     */
    @Transactional
    public void deductLoyaltyPoints(String idStr, int points) {
        if (idStr == null) {
            logger.error("Loyalty points update failed: ID cannot be null");
            throw new IllegalArgumentException("ID cannot be null");
        }
        if (points < 0) {
            logger.error("Loyalty points update failed: Points to deduct cannot be negative");
            throw new IllegalArgumentException("Points to deduct cannot be negative");
        }
        if (points == 0 ) {
            logger.error("Loyalty points update failed: Points to deduct cannot be O");
            throw new IllegalArgumentException("Points to deduct cannot be 0");
        }
        int currentPoints = getLoyaltyPoints(idStr);
        if (currentPoints < points) {
            logger.error("Loyalty points update failed: Not enough points to deduct");
            throw new IllegalArgumentException("Not enough points to deduct");
        }
        ObjectId id = new ObjectId(idStr);
        userRepository.updateLoyaltyPoints(id, -points);
    }

    /**
     * Retrieves the loyalty points of a user by their ID.
     * @param idStr the ID of the user
     * @return the loyalty points of the user
     */
    public int getLoyaltyPoints(String idStr) {
        ObjectId id = new ObjectId(idStr);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("No user found with ID: {}", id.toHexString());
                    return new IllegalArgumentException("No user found with this ID");
                });

        return user.getLoyaltyPoints();
    }
}