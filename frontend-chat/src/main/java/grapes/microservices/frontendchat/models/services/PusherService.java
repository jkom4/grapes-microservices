package grapes.microservices.frontendchat.models.services;

import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;
import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.dto.MessageDTO;
import grapes.microservices.frontendchat.models.dto.MessageMapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;


public class PusherService implements IPusherService {
    private static Gson gson;
    private final Pusher pusher;

    // observers
    private ObjectProperty<Message> receivedMessageObserver;

    @Override
    public void setReceivedMessageObserver(ObjectProperty<Message> receivedMessageObserver) {
        this.receivedMessageObserver = receivedMessageObserver;
    }

    public PusherService(String appKey, String cluster ) {
        gson = new Gson();
        // 1. Configure Pusher
        PusherOptions options = new PusherOptions().setCluster(cluster).setEncrypted(true);
        this.pusher = new Pusher(appKey, options);
    }

    @Override
    public void connect() {
        System.out.println("[Pusher] Connected");
        pusher.connect(new ConnectionEventListener() {
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                System.out.println("State changed to " + change.getCurrentState() +
                        " from " + change.getPreviousState());
            }

            @Override
            public void onError(String message, String code, Exception e) {
                System.err.println("There was a problem connecting!");
            }
        });
    }

    @Override
    public void subscribe(String channelName) {
        System.out.println("[Pusher] Subscribed to: " + channelName);
        Channel channel = pusher.subscribe(channelName);
        channel.bind("new message", (event) -> {
            System.out.println("[Pusher] Received event with data: " + event.getData());
            var dto = gson.fromJson(event.getData(), MessageDTO.class);
            receivedMessageObserver.set(MessageMapper.toEntity(dto));
        });

    }

    @Override
    public void unsubscribe(String channelName) {
        System.out.println("[Pusher] Unsubscribed to: " + channelName);
        pusher.unsubscribe(channelName);
    }

    @Override
    public void disconnect() {
        System.out.println("[Pusher] Disconnected");
        pusher.disconnect();
    }
}
