package grapes.microservices.authservice.services;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.repositories.UserRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(new ObjectId("67e13f0735d02563d11c04b6"));
        user.setFirstName("John");
        user.setEmail("test@example.com");
        user.setPassword("Valid123@");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User registeredUser = userService.registerUser(user);

        assertNotNull(registeredUser);
        assertEquals("John", registeredUser.getFirstName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));

        assertEquals("Already exists an account with this email", thrown.getMessage());
    }

    @Test
    void testRegisterUser_WeakPassword() {
        user.setPassword("weak");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(user));

        assertEquals("Password is not strong enough : it must contain at least one uppercase letter, one lowercase letter, one digit, one special character and must be at least 8 characters long", thrown.getMessage());
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));

        User retrievedUser = userService.getUserById(String.valueOf(user.getId()));

        assertNotNull(retrievedUser);
        assertEquals(user.getId(), retrievedUser.getId());
    }

    @Test
    void testGetUserById_UserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.getUserById(String.valueOf(user.getId())));

        assertEquals("No user found with this ID", thrown.getMessage());
    }

    @Test
    void testGetUserByEmail_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));

        User retrievedUser = userService.getUserByEmail(user.getEmail());

        assertNotNull(retrievedUser);
        assertEquals(user.getEmail(), retrievedUser.getEmail());
    }

    @Test
    void testGetUserByEmail_UserNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.getUserByEmail(user.getEmail()));

        assertEquals("No user found with this email", thrown.getMessage());
    }

    @Test
    void testEditUser_Success() {
        User updatedUser = new User();
        updatedUser.setFirstName("Jack");
        ObjectId id = new ObjectId("67e13f0735d02563d11c04b6");
        updatedUser.setId(id);

        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User editedUser = userService.editUser(updatedUser);

        assertNotNull(editedUser);
        assertEquals("Jack", editedUser.getFirstName());
    }

    @Test
    void testEditUser_PasswordNotStrongEnough() {
        User updatedUser = new User();
        ObjectId id = new ObjectId("67e13f0735d02563d11c04b6");
        updatedUser.setId(id);
        updatedUser.setPassword("weak");

        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.editUser(updatedUser));

        assertEquals("Password is not strong enough", thrown.getMessage());
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));

        userService.deleteUser(String.valueOf(user.getId()));

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(String.valueOf(user.getId())));

        assertEquals("No user found with this ID", thrown.getMessage());
    }
}
