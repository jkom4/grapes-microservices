package grapes.microservices.frontendchat.models.services;

import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IGrapesApi {
    /** Fetches the list of available chat topics from the API. */
    CompletableFuture<List<Topic>> fetchTopics(); // Retrieve topics from the API

    /** Sends a message through the API (for wider distribution or persistence). */
    void sendMessage(Topic topic, Message message); // Send message via API

    // Potentially other methods (authentication, fetching history, etc.)
}