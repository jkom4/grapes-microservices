package grapes.microservices.authservice.mapper;

import grapes.microservices.authservice.dto.UserDTO;
import grapes.microservices.authservice.models.Gender;
import grapes.microservices.authservice.models.Role;
import grapes.microservices.authservice.models.User;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    private final UserMapper userMapper = UserMapper.INSTANCE;


    @Test
    void shouldMapUserToDTO() {
        User user = new User();
        user.setId(new ObjectId("65f1a9a2a9b77b1e37bcabc1"));
        user.setName("Doe");
        user.setFirstName("John");
        user.setBirthDate(new Date(1000000000000L));

        UserDTO dto = userMapper.toDTO(user);

        assertEquals("65f1a9a2a9b77b1e37bcabc1", dto.getId());
        assertEquals("Doe", dto.getName());
        assertEquals("John", dto.getFirstName());
    }

    @Test
    void computeAge_forExactYears() {
        LocalDate birthDate = LocalDate.of(1995, 3, 25);
        Date date = Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        double age = userMapper.computeAge(date);

        // We expect him to be 30 years old if it's 2025, and he has already passed his birthday
        assertEquals(30.0, age, 0.1);
    }

    @Test
    void computeAge_withLeapYear() {
        // With a leap year, the age should be 25.0 in 2025
        LocalDate birthDate = LocalDate.of(2000, 2, 29);
        Date date = Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        double age = userMapper.computeAge(date);

        // In 2025, the age should be 25.0, because February 29, 2000 is not an exact date
        assertEquals(25.0, age, 0.1);
    }

    @Test
    void computeAge_forUnderage() {
        // Minor born in 2007, should be 18 years old in 2025
        LocalDate birthDate = LocalDate.of(2007, 3, 25);
        Date date = Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        double age = userMapper.computeAge(date);

        assertEquals(18.0, age, 0.1);
    }

    @Test
    void computeAge_withNoBirthDate() {
        // Test for a null birthdate (although in production this should not happen)
        assertThrows(IllegalArgumentException.class, () -> userMapper.computeAge(null));
    }

    @Test
    void toEntity_withValidDTO() {
        // Given a valid UserDTO
        UserDTO dto = new UserDTO();
        dto.setBankId("bank123");
        dto.setName("John");
        dto.setFirstName("Doe");
        dto.setPassword("password");
        dto.setEmail("john.doe@example.com");
        dto.setEmailVerified(true);
        dto.setPhoneNumber("1234567890");
        dto.setPhoneVerified(true);
        dto.setNationalId("12345");
        dto.setBirthDate(new Date(1000000000000L));  // Sample birthdate (example timestamp)
        dto.setGender(Gender.MALE);
        dto.setCardNumber("1111-2222-3333-4444");
        dto.setPinCode("1234");
        dto.setRole(Role.valueOf("USER"));
        dto.setProfession("Engineer");

        User user = userMapper.toEntity(dto);

        assertEquals("bank123", user.getBankId());
        assertEquals("John", user.getName());
        assertEquals("Doe", user.getFirstName());
        assertEquals("password", user.getPassword());
        assertEquals("john.doe@example.com", user.getEmail());
        assertTrue(user.isEmailVerified());
        assertEquals("1234567890", user.getPhoneNumber());
        assertTrue(user.isPhoneVerified());
        assertEquals("12345", user.getNationalId());
        assertEquals("MALE", user.getGender().name());
        assertEquals("1111-2222-3333-4444", user.getCardNumber());
        assertEquals("1234", user.getPinCode());
        assertEquals(Role.valueOf("USER"), user.getRole());
        assertEquals("Engineer", user.getProfession());
        assertEquals(dto.getDeliveryAddress(), user.getDeliveryAddress());
        assertEquals(dto.getBillingAddress(), user.getBillingAddress());
    }
}
