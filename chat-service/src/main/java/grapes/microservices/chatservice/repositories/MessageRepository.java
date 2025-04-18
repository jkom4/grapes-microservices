package grapes.microservices.chatservice.repositories;

import grapes.microservices.chatservice.models.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByChatId(String chatId);
}
