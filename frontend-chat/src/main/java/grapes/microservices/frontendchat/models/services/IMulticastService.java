package grapes.microservices.frontendchat.models.services;

import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;
import javafx.beans.property.ObjectProperty;

import java.io.IOException;

public interface IMulticastService {
    ObjectProperty<Message> getMessageReveiceObserver();

    /** start multicast service on initially specified port**/
    void init() throws IOException;

    /** **/
    void close();

    /** Joins a specific multicast group/topic. */
    void joinTopic(Topic topic) throws IOException;

    /** Leaves a specific multicast group/topic. */
    void leaveTopic(Topic topic) throws IOException;

    /** Sends a message via multicast to a specific topic. */
    void sendMessage(Topic topic, Message message) throws IOException;
}