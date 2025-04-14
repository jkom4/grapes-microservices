package grapes.microservices.frontendchat.models.services;

import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;
import grapes.microservices.frontendchat.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GrapesApi implements IGrapesApi {
    private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        Thread t = Executors.defaultThreadFactory().newThread(runnable);
        t.setDaemon(true); // Allow application to exit even if threads are running
        return t;
    });

    @Override
    public CompletableFuture<List<Topic>> fetchTopics() {
        return CompletableFuture.supplyAsync(() -> {
            // --- Simulate Network Delay ---
            try {
                System.out.println("[GrapesApi] loading topics");
                TimeUnit.SECONDS.sleep(2); // Simulate 2 seconds delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                System.err.println("[GrapesApi] Fetch interrupted!");
                return new ArrayList<>(); // Return empty list on interruption
            }

            System.out.println("[GrapesApi] topics loaded");
            return List.of(
                    new Topic(1, "General", "Hi Can someone help me"),
                    new Topic(2, "Conseil fruits de saison", "Ok thanks for the help"),
                    new Topic(3, "Problème livraison", "Ok thanks for the help"),
                    new Topic(254, "Suggestion", "Ok thanks for the help "),
                    new Topic(255, "Question sur les produits", "I have a problem with this article")
            );
        }, executor);
    }

    @Override
    public CompletableFuture<Void> postMessage(Topic topic, Message message) {
        // Simulate an asynchronous API call
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(200); // Simulate network latency
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[GrapesApi] Send message interrupted.");
            }
            System.out.println("[GrapesApi] Message sent via API to topic " + topic.getMulticastGroup() + ": " + message.content());
        });
        // TODO: Replace with actual HTTP client call
    }

    @Override
    public CompletableFuture<List<Message>> fetchMessages(int topicId) {
        System.out.println("[GrapesApi] loading messages");
        return CompletableFuture.supplyAsync(() -> {
            // --- Simulate Network Delay ---
            try {
                TimeUnit.SECONDS.sleep(2); // Simulate 2 seconds delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                System.err.println("[GrapesApi] Fetch interrupted!");
                return new ArrayList<>(); // Return empty list on interruption
            }

            System.out.println("[GrapesApi] messages loaded");
            return List.of(
                    new Message(1, new User(1, "Jean"), "Salut", LocalDateTime.now()),
//                    new Message(1, new User(1, "Marc"), "C'est moi", LocalDateTime.now()),
//                    new Message(1, new User(10, "Nasser"), "Vous faites quoi", LocalDateTime.now()),
//                    new Message(1, new User(10, "Nasser"), "Vous faites quoi", LocalDateTime.now()),
//                    new Message(1, new User(10, "Nasser"), "Vous faites quoi", LocalDateTime.now()),
//                    new Message(1, new User(10, "Nasser"), "Vous faites quoi", LocalDateTime.now()),
//                    new Message(1, new User(10, "Nasser"), "Vous faites quoi", LocalDateTime.now()),
//                    new Message(1, new User(10, "Nasser"), "Vous faites quoi", LocalDateTime.now()),
//                    new Message(1, new User(10, "Nasser"), "Vous faites quoi", LocalDateTime.now()),
                    new Message(1, new User(1, "Jean"), "Salut, j'ai un problème avec la livraison, le produit n'est pas arrivé", LocalDateTime.now()),
                    new Message(1, new User(2, "Vendeur"), "Un instant, je vais vérifier", LocalDateTime.now().plusMinutes(1)),
                    new Message(1, new User(10, "Jack"), "Pareil, On me dit que la livraison est arrivée mais rien", LocalDateTime.now().plusMinutes(2)),
                    new Message(1, new User(2, "Vendeur"), "Ok, je vais voir ", LocalDateTime.now().plusMinutes(3))
            );
        }, executor);
    }

    @Override
    public CompletableFuture<User> authUser(String token) {
        return CompletableFuture.supplyAsync(() -> {
            // --- Simulate Network Delay ---
            try {
                System.out.println("[GrapesApi] checking user auth token");
                TimeUnit.SECONDS.sleep(2); // Simulate 2 seconds delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                System.err.println("SERVICE: Fetch interrupted!");
                return null; // Return empty list on interruption
            }

            System.out.println("[GrapesApi] user auth token is valid");
            return new User(10, "Nasser");
        }, executor);
    }
}