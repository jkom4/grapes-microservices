package grapes.microservices.frontendchat.models.services;

import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;
import grapes.microservices.frontendchat.models.User;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Mock implementation of MulticastService for testing without actual network.
 */
public class MulticastService implements IMulticastService {
    private Consumer<Message> messageConsumer;
    private Topic currentTopic;
    private ScheduledExecutorService mockMessageExecutor;

    public MulticastService() {
        // Simulate receiving random messages in the current topic
        mockMessageExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // Allows the application to exit even if this thread is running
            return t;
        });

        mockMessageExecutor.scheduleAtFixedRate(() -> {
            if (messageConsumer != null && currentTopic != null) {
                Message fakeMsg = new Message(
                        currentTopic.id(),
                        new User(1, "OtherUser"),
                        "Received multicast sim message " + System.currentTimeMillis(),
                        LocalDateTime.now()
                );
                // !! IMPORTANT: The consumer call should happen on the JavaFX thread.
                // This responsibility is delegated to the ViewModel which uses Platform.runLater.
                if(messageConsumer != null) {
                    messageConsumer.accept(fakeMsg);
                }
            }
        }, 5, 10, TimeUnit.SECONDS); // Send a simulated message every 10 seconds
    }

    @Override
    public void joinTopic(Topic topic) {
        System.out.println("[MockMulticast] Joining topic: " + topic);
        this.currentTopic = topic;
        // TODO: Implement actual multicast group joining logic here
    }

    @Override
    public void leaveTopic(Topic topic) {
        System.out.println("[MockMulticast] Leaving topic: " + topic);
        if (this.currentTopic != null && this.currentTopic.equals(topic)) {
            this.currentTopic = null;
        }
        // TODO: Implement actual multicast group leaving logic here
    }

    @Override
    public void sendMessage(Topic topic, Message message) {
        System.out.println("[MockMulticast] Sending to topic " + topic.id() + ": " + message.content());
        // TODO: Implement actual multicast message sending logic here
    }

    @Override
    public void setMessageReceiver(Consumer<Message> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void startListening() {
        System.out.println("[MockMulticast] Start Listening Simulation...");
        // TODO: Initialize actual multicast socket and listening thread here
    }

    @Override
    public void stopListening() {
        System.out.println("[MockMulticast] Stop Listening Simulation...");
        mockMessageExecutor.shutdown();
        // TODO: Properly close socket and stop listening thread here
    }
}