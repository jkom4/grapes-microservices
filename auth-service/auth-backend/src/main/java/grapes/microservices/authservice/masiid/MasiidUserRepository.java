package grapes.microservices.authservice.masiid;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MasiidUserRepository extends MongoRepository<MasiIdUser, String> {
}
