package grapes.microservices.authservice.mapper;

import grapes.microservices.authservice.dto.UserDTO;
import grapes.microservices.authservice.models.User;
import org.bson.types.ObjectId;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToObjectId")
    @Mapping(source = "loyaltyPoints", target = "loyaltyPoints")
    User toEntity(UserDTO dto);

    @Mapping(target = "age", expression = "java(computeAge(user.getBirthDate()))")
    @Mapping(source = "id", target = "id", qualifiedByName = "objectIdToString")
    UserDTO toDTO(User user);

    @Named("stringToObjectId")
    static ObjectId stringToObjectId(String id) {
        return id != null ? new ObjectId(id) : null;
    }

    @Named("objectIdToString")
    static String objectIdToString(ObjectId id) {
        return id != null ? id.toHexString() : null;
    }

    List<UserDTO> toDTOList(List<User> users);


    /**
     * Compute the age based on the birthDate
     * @param birthDate the birthDate
     * @return the age
     */
    default int computeAge(Date birthDate) {
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
        double ageInYears = years + ((months * 30.4375 + days) / totalDaysInYear);
        return (int) Math.round(ageInYears);
    }
}