package grapes.microservices.authservice.services;

import java.util.Base64;
import java.util.regex.Pattern;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    /**
     * Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character
     * and must be at least 8 characters long
     */
    private static final String PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";


    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    /**
     * Register a new user and verify the password strength and email uniqueness
     * Hash the password before saving it with bcrypt
     * @param user the user to register
     * @return the registered user
     */
    public User registerUser(User user) throws IllegalArgumentException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Already exists an account with this email");
        }
        if (!isPasswordStrong(user.getPassword())) {
            throw new IllegalArgumentException("Password is not strong enough : it must contain at least one uppercase letter, one lowercase letter, one digit, one special character and must be at least 8 characters long");
        }
        if (!pattern.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Email is not valid");
        }
        user.encryptData();
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
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
            throw new IllegalArgumentException("ID cannot be null");
        }
        ObjectId id = new ObjectId(idStr);
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No user found with this ID"));
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
            throw new IllegalArgumentException("Email cannot be null");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with this email"));
    }

    /**
     * Edits the information of an existing user.
     *
     * @param updatedUser The updated user information.
     * @return The updated user.
     * @throws IllegalArgumentException if no user is found with the provided ID.
     */
    public User editUser(User updatedUser) throws IllegalArgumentException {
        User user;
        if (updatedUser.getId() != null) {
            user = getUserById(String.valueOf(updatedUser.getId()));
            System.out.println("1");
        } else if (updatedUser.getEmail() != null){
            user = getUserByEmail(updatedUser.getEmail());
            System.out.println("2");
        } else {
            System.out.println("3");
            throw new IllegalArgumentException("ID or email cannot be null");
        }

        user.update(updatedUser);
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            if (!isPasswordStrong(updatedUser.getPassword())) {
                throw new IllegalArgumentException("Password is not strong enough");
            }
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(user);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param idStr The ID of the user to delete.
     * @throws IllegalArgumentException if no user is found with the provided ID.
     */
    public void deleteUser(String idStr) throws IllegalArgumentException {
        if (idStr == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        User user = getUserById(idStr);
        userRepository.delete(user);
    }


    /**
     * Check if the password is strong enough
     * @param password the password to check
     * @return true if the password is strong enough
     */
    private boolean isPasswordStrong(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }}