package grapes.microservices.authservice.repositories;

import grapes.microservices.authservice.models.User;
import jakarta.validation.constraints.NotNull;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    Optional<User> findByEmail(@NotNull String email);
    boolean existsByEmail(@NotNull String email);

    boolean existsByPhoneNumber(@NotNull String phone);

    boolean existsByNationalId(@NotNull String nationalId);
    boolean existsById(@NotNull ObjectId id);

    /**
     * Updates the loyalty points of a user by their ID.
     * @param id the ID of the user
     * @param points the number of points to add/subtract
     */
    default void updateLoyaltyPoints(@NotNull ObjectId id, int points) {
        findById(id).ifPresent(user -> {
            user.setLoyaltyPoints(user.getLoyaltyPoints() + points);
            save(user);
        });
    }
}