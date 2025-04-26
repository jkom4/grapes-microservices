package grapes.microservices.frontendchat.models.services;

import grapes.microservices.frontendchat.models.Message;
import javafx.beans.property.ObjectProperty;

public interface IPusherService {
    void setReceivedMessageObserver(ObjectProperty<Message> receivedMessageObserver);

    void connect();

    void subscribe(String channelName);

    void unsubscribe(String channelName);

    void disconnect();
}
