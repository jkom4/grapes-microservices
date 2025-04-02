package grapes.microservices.authservice.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTest {

    private User user;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() throws Exception {
        passwordEncoder = mock(PasswordEncoder.class);
        user = new User();
        user.setPassword("correctPassword");
        user.setName("John");
        user.setFirstName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPhoneNumber("1234567890");
        user.setNationalId("11111111111");
        user.encryptUser();
    }

    @Test
    public void userCreation() {
        assertEquals("John", user.getName());
        assertEquals("Doe", user.getFirstName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhoneNumber());
    }

    @Test
    void verifyPassword_CorrectPassword_ReturnsTrue() {
        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        assertTrue(user.verifyPassword("correctPassword"));
    }

    @Test
    void verifyPassword_IncorrectPassword_ThrowsException() {
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);
        assertFalse(user.verifyPassword("wrongPassword"));
    }
}