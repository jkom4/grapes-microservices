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
    boolean existsById(@NotNull ObjectId id);
}