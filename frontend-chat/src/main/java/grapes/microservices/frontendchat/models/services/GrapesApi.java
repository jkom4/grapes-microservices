package grapes.microservices.frontendchat.models.services;

import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Mock implementation of ApiService for testing without a real backend.
 */
public class GrapesApi implements IGrapesApi {
    private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        Thread t = Executors.defaultThreadFactory().newThread(runnable);
        t.setDaemon(true); // Allow application to exit even if threads are running
        return t;
    });

    private final List<Topic> topics = List.of(
            new Topic(1, "General", "Hi Can someone help me"),
            new Topic(2, "Delivery", "Ok thanks for the help"),
            new Topic(3, "Discount", "I have a problem with this article")
    );

    @Override
    public CompletableFuture<List<Topic>> fetchTopics() {
        return CompletableFuture.supplyAsync(() -> {
            // --- Simulate Network Delay ---
            try {
                System.out.println("SERVICE: Starting simulated API call...");
                TimeUnit.SECONDS.sleep(2); // Simulate 2 seconds delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                System.err.println("SERVICE: Fetch interrupted!");
                return new ArrayList<>(); // Return empty list on interruption
            }

            return topics;
        }, executor);
    }

    @Override
    public void sendMessage(Topic topic, Message message) {
        // Simulate an asynchronous API call
        CompletableFuture.runAsync(() -> {
            System.out.println("[GrapesApi] Sending message via API to topic " + topic.getMulticastGroup() + ": " + message.content());
            try {
                Thread.sleep(200); // Simulate network latency
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[MockApi] Send message simulation interrupted.");
            }
            System.out.println("[MockApi] API 'Send Message' response received (simulated).");
        });
        // TODO: Replace with actual HTTP client call
    }
}