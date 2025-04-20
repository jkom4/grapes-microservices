package grapes.microservices.frontendchat.models.services;

import com.google.gson.Gson;
import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;
import grapes.microservices.frontendchat.models.User;
import grapes.microservices.frontendchat.models.dto.*;
import grapes.microservices.frontendchat.models.exceptions.MyApiBadRequestException;
import grapes.microservices.frontendchat.models.exceptions.MyApiConnectionException;
import grapes.microservices.frontendchat.models.shared.UserSession;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class GrapesApi implements IGrapesApi {
    public final String base;
    public static final Gson GSON = new Gson();

    public GrapesApi(String base) {
        this.base = base;
    }

    private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
        Thread t = Executors.defaultThreadFactory().newThread(runnable);
        t.setDaemon(true); // Allow application to exit even if threads are running
        return t;
    });

    @Override
    public CompletableFuture<User> getUser() {
        return CompletableFuture.supplyAsync(() -> {
            // Build request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(base + "user"))
                    .header("Authorization", "Bearer " + UserSession.getToken())
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            // Send request
            var response = sendRequest(request);

            if (response.statusCode() == 403) {
                throw new CompletionException(new MyApiBadRequestException("Your request is unauthorized"));
            }

            // convert response
            System.out.println("[GrapesApi] user loaded");
            UserDTO user = GSON.fromJson(response.body(), UserDTO.class);
            return UserMapper.toEntity(user);
//            return new User("1", "Jean");
        }, executor);
    }

    @Override
    public CompletableFuture<List<Topic>> fetchTopics() {
        return CompletableFuture.supplyAsync(() -> {
            // Build request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(base + "topics"))
                    .header("Authorization", "Bearer " + UserSession.getToken())
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            // Send request
            var response = sendRequest(request);

            // convert response
            System.out.println("[GrapesApi] topics loaded");
            TopicDTO[] topicsArray = GSON.fromJson(response.body(), TopicDTO[].class);
            List<TopicDTO> topics = Arrays.asList(topicsArray);
            return TopicMapper.toEntityList(topics);
        }, executor);
    }

    @Override
    public CompletableFuture<List<Message>> fetchMessages(int topicId) {
        System.out.println("[GrapesApi] loading messages");
        return CompletableFuture.supplyAsync(() -> {
            // Build request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(base + "topic/" + topicId + "/messages"))
                    .header("Authorization", "Bearer " + UserSession.getToken())
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            // Send request
            var response = sendRequest(request);

            // convert response
            System.out.println("[GrapesApi] messages loaded");
            MessageDTO[] messagesArray = GSON.fromJson(response.body(), MessageDTO[].class);
            List<MessageDTO> messages = Arrays.asList(messagesArray);
            return MessageMapper.toEntityList(messages);
        }, executor);
    }

    @Override
    public void postMessage(Topic topic, Message message) {
        CompletableFuture.runAsync(() -> {
            try {
                // Convert the Message object to JSON using GSON
                String jsonMessage = GSON.toJson(MessageMapper.toDTO(message));

                // Build the HTTP POST request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(base + "topic/" + topic.id() + "/message"))
                        .header("Content-Type", "application/json")
                         .header("Authorization", "Bearer " + UserSession.getToken())
                        .POST(HttpRequest.BodyPublishers.ofString(jsonMessage))
                        .build();

                // Send the request asynchronously
                HttpClient client = HttpClient.newHttpClient();
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(HttpResponse::body) // Process the response body
                        .thenAccept(responseBody -> System.out.println("Response: " + responseBody))
                        .exceptionally(e -> {
                            System.err.println("Request failed: " + e.getMessage());
                            return null;
                        });
            } catch (Exception e) {
                System.err.println("Error building or sending the request: " + e.getMessage());
            }
        });
    }

    private HttpResponse<String> sendRequest(HttpRequest request) {
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            System.err.println("[GrapesApi] request interrupted!");
            throw new RuntimeException("Error: " + e.getMessage(), e); // Return empty list on interruption
        } catch (ConnectException e) {
            // I cannot throw my exception here because we are in a lambda expression that doesn't allow it, so
            //  instead we put it in another exception that is allowed.
            var message = "Cannot connect to the Chat API! Is the API up? Is the device connected to Internet?";
            throw new CompletionException(new MyApiConnectionException(message));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
}