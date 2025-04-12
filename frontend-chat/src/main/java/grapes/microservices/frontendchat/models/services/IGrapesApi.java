package grapes.microservices.frontendchat.models.services;

import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;
import grapes.microservices.frontendchat.models.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IGrapesApi {
    /** Fetches the list of all topics */
    CompletableFuture<List<Topic>> fetchTopics();

    /** Sends a message through the API (for wider distribution or persistence). */
    CompletableFuture<Void> postMessage(Topic topic, Message message);

    /** Fetches the list of messages sent on a topic. */
    CompletableFuture<List<Message>> fetchMessages(int topicId);

    /** Authenticate the user with given token, returns user object. */
    CompletableFuture<User> authUser(String token);
}