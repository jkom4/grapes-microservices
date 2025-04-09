package grapes.microservices.frontendchat.viewmodels;


import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;
import grapes.microservices.frontendchat.models.User;
import grapes.microservices.frontendchat.models.services.IGrapesApi;
import grapes.microservices.frontendchat.models.services.IMulticastService;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * ViewModel for the Chat View.
 * Acts as an intermediary between the View (FXML) and the Model (Services).
 * Exposes data via JavaFX Properties for binding and handles user actions.
 */
public class ChatViewModel {

    // --- Dependencies ---
    private final IMulticastService multicastService;
    private final IGrapesApi apiService;

    // --- JavaFX Properties for View Binding ---
    private final ObservableList<Topic> topics = FXCollections.observableArrayList();
    public ObservableList<Topic> getTopics() {
        return topics;
    }


    // Holds the currently selected topic object
    private final ObjectProperty<Topic> currentTopic = new SimpleObjectProperty<>(null);
    // Bound to the status Label
    private final StringProperty status = new SimpleStringProperty("Initializing...");

    // --- Internal State ---
    private final User currentUser; // Should be set via login, config, etc.

    // --- Constructor ---
    public ChatViewModel(IMulticastService multicastService, IGrapesApi apiService) {
        this.multicastService = multicastService;
        this.apiService = apiService;

        // Example: Set a default user. Replace with proper user management.
        this.currentUser = new User("DefaultUser");
    }

    public void fetchTopics() {
        this.apiService.fetchTopics()
                .whenComplete((fetchedMessages, error) -> {
                    // This block runs after the async operation completes
                    // IMPORTANT: Update UI elements on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        if (error != null) {
                            // Handle error case
                            System.err.println("VIEWMODEL: Error fetching messages: " + error.getMessage());
                            error.printStackTrace(); // Print stack trace for debugging
                        } else if (fetchedMessages != null) {
                            // Handle success case
                            System.out.println("VIEWMODEL: Messages received, updating list.");
                            topics.addAll(fetchedMessages);
                        }
                    });
                });
    }

    /**
     * Cleans up resources when the application is closing.
     */
    public void shutdown() {
        status.set("Shutting down...");
        System.out.println("ViewModel shutdown initiated.");
        Topic topic = currentTopic.get();
        if(topic != null) {
            // Leave the current multicast group if joined
            try {
                multicastService.leaveTopic(topic);
            } catch (Exception e) {
                System.err.println("Error leaving topic during shutdown: " + e.getMessage());
            }
        }
        // Stop the multicast listener thread/service
        try {
            multicastService.stopListening();
        } catch (Exception e) {
            System.err.println("Error stopping multicast listener during shutdown: " + e.getMessage());
        }
        System.out.println("ViewModel shutdown complete.");
        // Add any other cleanup needed (e.g., closing API connections if kept open)
    }
}