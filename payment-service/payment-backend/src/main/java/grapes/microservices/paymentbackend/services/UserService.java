package grapes.microservices.paymentbackend.services;


import grapes.microservices.paymentbackend.models.User;
import grapes.microservices.paymentbackend.repositories.UserRepository;
import grapes.microservices.paymentbackend.utils.PasswordManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import grapes.microservices.paymentbackend.utils.PasswordManager;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordManager passwordManager;

    @Autowired
    public UserService(UserRepository userRepository, PasswordManager passwordManager) {
        this.userRepository = userRepository;
        this.passwordManager = passwordManager;
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
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