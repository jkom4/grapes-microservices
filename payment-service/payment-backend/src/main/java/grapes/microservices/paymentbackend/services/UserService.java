package grapes.microservices.paymentbackend.services;

import grapes.microservices.paymentbackend.dto.UserDTO;
import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.repositories.UserRepository;
import grapes.microservices.paymentbackend.utils.PasswordManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordManager passwordManager;

    /**
     * Verify if a user's credentials are valid
     * @param login the user's login
     * @param password the user's password
     * @return true if valid, false otherwise
     */
    public boolean verifyCredentials(String login, String password) {
        Optional<User> userOpt = userRepository.findByLogin(login);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        String saltedPassword = passwordManager.saltPassword(password);

        try {
            String hashedPassword = passwordManager.hashPassword(saltedPassword);
            return hashedPassword.equals(user.getPassword());
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    /**
     * Find a user by login
     * @param login the user's login
     * @return the user if found, empty otherwise
     */
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    /**
     * Create a new user
     * @param userDTO the user data
     * @return the created user
     */
    public User createUser(UserDTO userDTO) throws NoSuchAlgorithmException {
        // Check if user already exists
        if (userRepository.existsByLogin(userDTO.getLogin())) {
            throw new IllegalArgumentException("User with login " + userDTO.getLogin() + " already exists");
        }

        // Salt and hash password
        String saltedPassword = passwordManager.saltPassword(userDTO.getPassword());
        String hashedPassword = passwordManager.hashPassword(saltedPassword);

        // Create and save user
        User user = new User(
                userDTO.getLogin(),
                hashedPassword,
                userDTO.getPhoneNumber()
        );

        return userRepository.save(user);
    }



    public boolean verifyUser(String login, String password) throws NoSuchAlgorithmException {
        Optional<User> userOpt = userRepository.findByLogin(login);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String saltedPassword = passwordManager.saltPassword(password);
            String hashedPassword = passwordManager.hashPassword(saltedPassword);
            System.out.println("[INFO] Hashed password: " + hashedPassword);

            return user.getPassword().equals(hashedPassword);
        }

        return false;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public boolean updateBalance(User user, double amount) {
        if (user.getAccountBalance() >= amount) {
            user.setAccountBalance(user.getAccountBalance() - amount);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Optional<User> findById(Long userId) {

        return null;
    }

}