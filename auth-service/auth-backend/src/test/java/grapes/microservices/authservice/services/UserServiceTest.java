package grapes.microservices.authservice.services;

import grapes.microservices.authservice.models.Address;
import grapes.microservices.authservice.models.Gender;
import grapes.microservices.authservice.models.Role;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.repositories.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Logger logger;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(logger.isInfoEnabled()).thenReturn(true);
        when(logger.isDebugEnabled()).thenReturn(true);
        when(logger.isErrorEnabled()).thenReturn(true);
        when(logger.isWarnEnabled()).thenReturn(true);

        Address address = new Address();
        user = new User();
        user.setId(new ObjectId("67e13f0735d02563d11c04b6"));
        user.setActive(true);
        user.setFirstName("John");
        user.setName("Doe");
        user.setBillingAddress(address);
        user.setDeliveryAddress(address);
        user.setEmail("test@example.com");
        user.setPassword("Valid123@");
        user.setGender(Gender.MALE);
        user.setPhoneNumber("+32000000");
        user.setPinCode("1234");
        user.setNationalId("12345678901");
        user.setBirthDate(new Date());
        user.setRole(Role.USER);
    }

    @Test
    void registerUser_Success() throws Exception {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User registeredUser = userService.registerUser(user);

        assertNotNull(registeredUser);
        assertEquals("John", registeredUser.getFirstName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));

        assertEquals("Already exists an account with this email", thrown.getMessage());
    }

    @Test
    void registerUser_WeakPassword() {
        user.setPassword("weak");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));

        assertTrue(thrown.getMessage().startsWith("Validation errors"));
    }


    @Test
    void getUserById_Success() {
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));

        User retrievedUser = userService.getUserById(String.valueOf(user.getId()), false);

        assertNotNull(retrievedUser);
        assertEquals(user.getId(), retrievedUser.getId());
    }

    @Test
    void getUserById_UserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.getUserById(String.valueOf(user.getId()), false));

        assertEquals("No user found with this ID", thrown.getMessage());
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));

        User retrievedUser = userService.getUserByEmail(user.getEmail());

        assertNotNull(retrievedUser);
        assertEquals(user.getEmail(), retrievedUser.getEmail());
    }

    @Test
    void getUserByEmail_UserNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.getUserByEmail(user.getEmail()));

        assertEquals("No user found with this email", thrown.getMessage());
    }

    @Test
    void editUser_Success() throws Exception {
        User updatedUser = new User();
        updatedUser.setFirstName("Jack");
        updatedUser.setPassword("Valid123@");
        updatedUser.setPinCode("1234");
        ObjectId id = new ObjectId("67e13f0735d02563d11c04b6");
        updatedUser.setId(id);

        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        //user.encryptUser();
        User editedUser = userService.updateUser("67e13f0735d02563d11c04b6", updatedUser);

        assertNotNull(editedUser);
        assertEquals("Jack", editedUser.getFirstName());
    }

    @Test
    void editUser_PasswordNotStrongEnough() {
        User updatedUser = new User();
        ObjectId id = new ObjectId("67e13f0735d02563d11c04b6");
        updatedUser.setId(id);
        updatedUser.setPassword("weakpassword");

        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.updateUser("67e13f0735d02563d11c04b6", updatedUser));

        assertEquals("Password verification failed", thrown.getMessage());
    }

    @Test
    void disableUser_Success() {
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));

        System.out.println(user.isActive());
        userService.disableUser(String.valueOf(user.getId()));
        System.out.println(user.isActive());
        assertFalse(user.isActive());
    }

    @Test
    void disableUser_UserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.disableUser(String.valueOf(user.getId())));

        assertEquals("No user found with this ID", thrown.getMessage());
    }
}
