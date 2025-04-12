package grapes.microservices.authservice.services;

import grapes.microservices.authservice.models.AuthMean;
import grapes.microservices.authservice.models.AuthMethod;
import grapes.microservices.authservice.models.Role;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.repositories.UserRepository;
import grapes.microservices.authservice.utils.AuthLogger;
import grapes.microservices.authservice.utils.exceptions.UnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static grapes.microservices.authservice.utils.validators.UserValidator.isValid;

/**
 * UserService provides CRUD operations for managing User entities.
 * This service handles user registration, retrieval, update, and deletion.
 * It ensures password encryption, email uniqueness, and user validation before processing.
 *
 * @author Cameron
 */
@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final TokenService tokenService;

    private static final Logger logger = LoggerFactory.getLogger(AuthLogger.class);

    @Value("${auth.service.required.age}")
    private static int REQUIRED_AGE;

    /**
     * Register a new user and verify the password strength and email uniqueness
     * Hash the password before saving it with bcrypt
     *
     * @param user the user to register
     * @return the registered user
     */
    @Transactional
    public User registerUser(User user) throws Exception {
        validateRegistrationCriteria(user);
        initializeDefaultUserValues(user);
        if (isValid(user)) {
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
    public User getUserById(String idStr, boolean safe) throws IllegalArgumentException {
        if (idStr == null) {
            logger.error("User retrieval failed: ID cannot be null");
            throw new IllegalArgumentException("ID cannot be null");
        }
        ObjectId id = new ObjectId(idStr);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("No user found with ID: {}", idStr);
                    return new IllegalArgumentException("No user found with this ID");
                });
        if (safe) {
            user.setPassword(null);
            user.setPinCode(null);
        }
        return user;
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
     * Password and PIN code cannot be changed here, use the appropriate methods for that.
     * @param updatedUser The updated user information.
     * @return The updated user.
     * @throws IllegalArgumentException if no user is found with the provided ID.
     */
    @Transactional
    public User updateUser(String idStr, User updatedUser) {
        User userToUpdate = getUserById(idStr, false);

        updatedUser.setPinCode(null);
        updatedUser.setPassword(null);
        checkPasswordAndPinHaveNotChanged(userToUpdate, updatedUser);
        checkAccountIsUnique(userToUpdate, updatedUser);

        userToUpdate.update(updatedUser);
        userToUpdate.setActive(true);
        userToUpdate.setUpdatedAt(new java.util.Date());
        List<String> tempValues = bypassEncryptedVerification(userToUpdate);
        if (isValid(userToUpdate)) {
            restoreEncryptedVerification(userToUpdate, tempValues);
            logger.info("User with ID: {} updated successfully", userToUpdate.getId());
            return userRepository.save(userToUpdate);
        }
        logger.error("User update failed: User with ID: {} is not valid", userToUpdate.getId());
        throw new IllegalArgumentException("User is not valid");
    }

    /**
     * Updates the password of a user.
     *
     * @param user the user to update
     * @param currentPassword the current password
     * @param newPassword the new password
     * @return the updated user
     */
    public User editPassword(User user, String currentPassword, String newPassword) {
        if (user == null) {
            logger.error("Password update failed: User cannot be null");
            throw new IllegalArgumentException("User cannot be null");
        }
        if (!user.verifyUserPassword(currentPassword)) {
            logger.error("Password update failed: Current password is incorrect.");
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (!User.isPasswordFormatValid(newPassword)) {
            logger.error("Password update failed: Invalid password format");
            throw new IllegalArgumentException("Password must contain at least 8 characters, including uppercase, lowercase, digits, and special characters");
        }
        if (user.verifyUserPassword(newPassword)) {
            logger.error("Password update failed: New password cannot be the same as the old password");
            throw new IllegalArgumentException("New password cannot be the same as the old password");
        }
        user.setUpdatedAt(new java.util.Date());
        user.setPasswordValid(true);
        user.encryptPassword(newPassword);
        return userRepository.save(user);
    }

    /**
     * Updates the PIN code of a user.
     * @param user the user to update
     * @param currentPin the current PIN code
     * @param newPin the new PIN code
     */
    public User editPin(User user, String currentPin, String newPin) throws Exception {
        if (user == null) {
            logger.error("Pin update failed: User cannot be null");
            throw new IllegalArgumentException("User cannot be null");
        }
        if (!user.verifyUserPin(currentPin)) {
            logger.error("Pin update failed: Current PIN is incorrect.");
            throw new IllegalArgumentException("Current PIN is incorrect.");
        }
        if  (!User.isPinFormatValid(newPin)) {
            logger.error("Pin update failed: Invalid PIN format");
            throw new IllegalArgumentException("PIN should be 4 digits");
        }
        if (user.verifyUserPin(newPin)) {
            logger.error("Pin update failed: New PIN cannot be the same as the old PIN");
            throw new IllegalArgumentException("New Pin cannot be the same as the old PIN");
        }
        user.encryptPinCode(newPin);
        return userRepository.save(user);
    }

    public List<User> getAllUsers(String token) {
        String role = tokenService.extractUserRole(token);
        if (!role.equals(Role.ADMIN.getRole())) {
            throw new UnauthorizedException("Only admin can access this endpoint");
        }
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new IllegalArgumentException("No users found");
        }
        return users;
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
        User user = getUserById(idStr, false);
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
        User user = getUserById(idStr, false);
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
        if (points == 0) {
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
        if (points == 0) {
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
     *
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

    /**
     * Initializes a new user with default values.
     *
     * @param user the user to initialize
     */
    private void initializeDefaultUserValues(User user) {
        user.setActive(true);
        user.setLoyaltyPoints(0);
        user.setPasswordValid(true);
        user.setCreatedAt(new java.util.Date());
        user.setUpdatedAt(new java.util.Date());
        Map<AuthMethod, AuthMean> authMeans = new HashMap<>();
        authMeans.put(AuthMethod.EMAIL, new AuthMean(false, null, 0));
        authMeans.put(AuthMethod.SMS, new AuthMean(false, null, 0));
        authMeans.put(AuthMethod.EID, new AuthMean(false, null, 0));
        user.setAuthMeans(authMeans);
    }

    /**
     * Bypass encrypted verification for a user because encrypted fields don't respect Jakarta validation
     *
     * @param user the user to bypass verification for
     * @return the list of bypassed fields
     */
    private List<String> bypassEncryptedVerification(User user) {
        String tempPassword = user.getPassword();
        String tempPinCode = user.getPinCode();
        user.setPassword("P@ssw0rd");
        user.setPinCode("1234");
        return List.of(tempPassword, tempPinCode);
    }

    /**
     * Restores the original values of the user after bypassing encrypted verification
     *
     * @param user       the user to restore
     * @param tempValues the list of bypassed fields
     */
    private void restoreEncryptedVerification(User user, List<String> tempValues) {
        user.setPassword(tempValues.get(0));
        user.setPinCode(tempValues.get(1));
    }

    /**
     * Validates the registration criteria for a user.
     * Checks if the email, phone number, and national ID are unique
     * @param user the user to validate
     */
    private void validateRegistrationCriteria(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.error("Registration failed: an account already exists with this email: {}", user.getEmail());
            throw new IllegalArgumentException("Already exists an account with this email");
        }
        if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            logger.error("Registration failed: an account already exists with this phone number: {}", user.getPhoneNumber());
            throw new IllegalArgumentException("Already exists an account with this phone number");
        }
        if (userRepository.existsByNationalId(user.getNationalId())) {
            logger.error("Registration failed: an account already exists with this national ID: {}", user.getNationalId());
            throw new IllegalArgumentException("Already exists an account with this national ID");
        }
        if (!user.hasRequiredAge(REQUIRED_AGE)) {
            logger.error("Registration failed: user must be at least 16 years old");
            throw new IllegalArgumentException("User must be at least 16 years old");
        }
        if (!user.nationalIdMatchesBirthDate()) {
            logger.error("Registration failed: national ID does not match birth date");
            throw new IllegalArgumentException("National ID does not match birth date");
        }
    }

    /**
     * Checks if the updated user has unique email and phone number
     * @param userToUpdate the user to update
     * @param updatedUser the updated user
     */
    private void checkAccountIsUnique(User userToUpdate, User updatedUser) {
        boolean phoneHasChanged = !userToUpdate.getPhoneNumber().equals(updatedUser.getPhoneNumber());
        boolean emailHasChanged = !userToUpdate.getEmail().equals(updatedUser.getEmail());

        if (emailHasChanged && userRepository.existsByEmail(updatedUser.getEmail())) {
            logger.error("Update failed: an account already exists with this email: {}", updatedUser.getEmail());
            throw new IllegalArgumentException("Already exists an account with this email");
        }
        if (phoneHasChanged && userRepository.existsByPhoneNumber(updatedUser.getPhoneNumber())) {
            logger.error("Update failed: an account already exists with this phone number: {}", updatedUser.getPhoneNumber());
            throw new IllegalArgumentException("Already exists an account with this phone number");
        }

        if (emailHasChanged) {
            userToUpdate.setEmailVerified(false);
        }
        if (phoneHasChanged) {
            userToUpdate.setPhoneVerified(false);
        }
    }

    /**
     * Checks if the password and PIN code have changed
     * @param userToUpdate the user to update
     * @param updatedUser the updated user
     */
    private void checkPasswordAndPinHaveNotChanged(User userToUpdate, User updatedUser) {
        boolean pinHasChanged = updatedUser.getPinCode() != null && !userToUpdate.verifyUserHashedPin(updatedUser.getPinCode());
        boolean passwordHasChanged = updatedUser.getPassword() != null && !userToUpdate.verifyUserPasswordFromHash(updatedUser.getPassword());

        if (pinHasChanged || passwordHasChanged) {
            throw new UnauthorizedException("Pin code or password cannot be changed here, please use the appropriate methods");
        }
    }
}