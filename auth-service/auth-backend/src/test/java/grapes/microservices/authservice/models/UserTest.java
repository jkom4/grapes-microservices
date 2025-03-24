package grapes.microservices.authservice.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    /**
     * Test the creation of a user
     */
    @Test
    public void testUserCreation() {
        User user = new User();
        user.setName("John");
        user.setFirstName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPhoneNumber("1234567890");

        assertEquals("John", user.getName());
        assertEquals("Doe", user.getFirstName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhoneNumber());
    }

}