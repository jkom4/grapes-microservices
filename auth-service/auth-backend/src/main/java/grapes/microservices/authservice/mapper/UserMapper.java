package grapes.microservices.authservice.mapper;

import grapes.microservices.authservice.dto.UserDTO;
import grapes.microservices.authservice.models.User;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

@Component
public class UserMapper {

    public User toEntity(@NotNull UserDTO dto) {
        User user = new User();
        if (dto.getId() != null) {
            user.setId(new ObjectId(dto.getId()));
        }
        user.setBankId(dto.getBankId());
        user.setName(dto.getName());
        user.setFirstName(dto.getFirstName());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());
        user.setEmailVerified(dto.isEmailVerified());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPhoneVerified(dto.isPhoneVerified());
        user.setNationalId(dto.getNationalId());
        user.setBirthDate(dto.getBirthDate());
        user.setGender(dto.getGender());
        user.setCardNumber(dto.getCardNumber());
        user.setPinCode(dto.getPinCode());
        user.setRole(dto.getRole());
        user.setProfession(dto.getProfession());
        user.setAuthMethods(dto.getAuthMethods());
        user.setDeliveryAddress(dto.getDeliveryAddress());
        user.setBillingAddress(dto.getBillingAddress());

        return user;
    }

    public UserDTO toDTO(@NotNull User user) {
        UserDTO dto = new UserDTO();
        if (user.getId() != null) {
            dto.setId(user.getId().toHexString());
        }
        dto.setBankId(user.getBankId());
        dto.setName(user.getName());
        dto.setFirstName(user.getFirstName());
        dto.setPassword(user.getPassword());
        dto.setEmail(user.getEmail());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setPhoneVerified(user.isPhoneVerified());
        dto.setNationalId(user.getNationalId());
        dto.setBirthDate(user.getBirthDate());
        dto.setGender(user.getGender());
        dto.setCardNumber(user.getCardNumber());
        dto.setPinCode(user.getPinCode());
        dto.setRole(user.getRole());
        dto.setProfession(user.getProfession());
        dto.setAuthMethods(user.getAuthMethods());
        dto.setDeliveryAddress(user.getDeliveryAddress());
        dto.setBillingAddress(user.getBillingAddress());

        return dto;
    }

    /**
     * Compute the age based on the birthDate
     * @param birthDate the birthDate
     * @return the age
     */
    double computeAge(java.util.Date birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("birthDate cannot be null");
        }

        LocalDate birthLocalDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = LocalDate.now();

        Period period = Period.between(birthLocalDate, now);
        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        int totalDaysInYear = now.isLeapYear() ? 366 : 365;

        // 365.25/12 ≈ 30.4375 -> mean number of days in a month
        return years + ((months * 30.4375 + days) / totalDaysInYear);
    }
}
