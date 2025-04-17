package grapes.microservices.chatservice.repositories;

import grapes.microservices.chatservice.models.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {
}
