package grapes.microservices.authservice.mapper;

import grapes.microservices.authservice.dto.EIDCardInfo;
import grapes.microservices.authservice.dto.UserDTO;
import grapes.microservices.authservice.models.User;
import grapes.microservices.authservice.models.Role;
import org.bson.types.ObjectId;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToObjectId")
    User toEntity(UserDTO dto);

    @Mapping(target = "age", expression = "java(computeAge(user.getBirthDate()))")
    @Mapping(source = "id", target = "id", qualifiedByName = "objectIdToString")
    UserDTO toDTO(User user);
    // Mapping using eID card data
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "bankId", constant = "DEFAULT_BANK")
    @Mapping(target = "name", source = "lastName")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "nationalId", source = "nationalId")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "age", expression = "java(computeAge(info.getBirthDate()))")
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "phoneVerified", constant = "false")
    @Mapping(target = "cardNumber", ignore = true)
    @Mapping(target = "pinCode", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "profession", ignore = true)
    @Mapping(target = "authMethods", ignore = true)
    @Mapping(target = "deliveryAddress", ignore = true)
    @Mapping(target = "billingAddress", ignore = true)
    UserDTO toDTOFromEID(EIDCardInfo info);

    @Named("stringToObjectId")
    static ObjectId stringToObjectId(String id) {
        return id != null ? new ObjectId(id) : null;
    }

    @Named("objectIdToString")
    static String objectIdToString(ObjectId id) {
        return id != null ? id.toHexString() : null;
    }

    /**
     * Compute the age based on the birthDate
     * @param birthDate the birthDate
     * @return the age
     */
    default double computeAge(Date birthDate) {
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

        // 30.4375 is the mean of number of days in a month
        return years + ((months * 30.4375 + days) / totalDaysInYear);
    }
}