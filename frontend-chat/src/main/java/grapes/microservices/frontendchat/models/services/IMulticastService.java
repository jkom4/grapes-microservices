package grapes.microservices.frontendchat.models.services;

import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;

import java.io.IOException;
import java.util.function.Consumer;

public interface IMulticastService {
    /** Joins a specific multicast group/topic. */
    void joinTopic(Topic topic) throws IOException;

    /** Leaves a specific multicast group/topic. */
    void leaveTopic(Topic topic);

    /** Sends a message via multicast to a specific topic. */
    void sendMessage(Topic topic, Message message) throws IOException;

    /**
     * Sets a callback function to be invoked when a message is received via multicast.
     * @param messageConsumer The function to handle the received message.
     */
    void setMessageReceiver(Consumer<Message> messageConsumer) throws IOException;

    /** Starts the general listening process for multicast messages (if needed globally). */
    void startListening();

    /** Stops the listening process and releases resources. */
    void stopListening();
}