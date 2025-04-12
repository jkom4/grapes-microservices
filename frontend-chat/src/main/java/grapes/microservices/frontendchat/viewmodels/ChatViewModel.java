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
import lombok.Getter;

import java.io.IOException;
import java.time.LocalDateTime;


/**
 * ViewModel for the Chat View.
 * Acts as an intermediary between the View (FXML) and the Model (Services).
 * Exposes data via JavaFX Properties for binding and handles user actions.
 */
public class ChatViewModel {

    // Services
    private final IMulticastService multicastService;
    private final IGrapesApi apiService;

    // Observers
    @Getter
    private final ObservableList<Topic> topicsObserver = FXCollections.observableArrayList();
    @Getter // Update current topic when user select a topic
    private final ObjectProperty<Topic> currentTopicObserver = new SimpleObjectProperty<>(null);
    @Getter // Update message list when new topic selected or message sent by user
    private final ObservableList<Message> messageListObserver = FXCollections.observableArrayList();
    @Getter // Display or hide messages loading animation when user select a topic
    private final SimpleBooleanProperty areMessagesLoading = new SimpleBooleanProperty(false);
    private final StringProperty status = new SimpleStringProperty("Initializing...");
    @Getter // Updated when user successfully authenticate
    private ObjectProperty<User> currentUserObserver = new SimpleObjectProperty<>(null);
    @Getter // Updated when user send messages, then actions are executed
    private SimpleStringProperty postedMessageObserver = new SimpleStringProperty("");

    // Constructor
    public ChatViewModel(IMulticastService multicastService, IGrapesApi apiService) {
        this.multicastService = multicastService;
        this.apiService = apiService;

        setObserversEvents();
    }

    private void setObserversEvents() {
        // When user send a message, it's added to the list + multicasted in local + notify the api
        postedMessageObserver.addListener((observable, oldValue, newValue) -> {
            // when user sends an empty message, nothing happens
            if (newValue.isEmpty()) return;

            // !!! message created in local!
            var topicId = currentTopicObserver.get().id();
            var message = new Message(topicId, currentUserObserver.get(), newValue, LocalDateTime.now());

            // 1. add user's own message to the list
            messageListObserver.add(message);
            // 2. multicast the message in local
            try {
                multicastService.sendMessage(currentTopicObserver.get(), message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 3. notify the api that a message was send
            apiService.postMessage(currentTopicObserver.get(), message);
        });

        // When user join/left a topic
        currentTopicObserver.addListener((observable, oldTopic, newTopic) -> {
            boolean joinedTopic = newTopic != null; // if topic == null, it means that user closed the topic
            boolean hasChangedTopic = oldTopic != null;

            if (hasChangedTopic) {
                multicastService.leaveTopic(oldTopic);
            }

            if (joinedTopic) {
                try {
                    multicastService.joinTopic(newTopic);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                multicastService.startListening();
            } else {
                multicastService.stopListening();
            }
        });
    }

    public void authUser(String token) {
        this.apiService.authUser(token)
                .whenComplete((data, error) -> {
                    Platform.runLater(() -> {
                        if (error != null) {
                            // Handle error case
                            throw new RuntimeException(error);
                        } else if (data != null) {
                            // Handle success case
                            currentUserObserver.set(data);
                        }
                    });
                });
    }

    public void fetchTopics() {
        this.apiService.fetchTopics()
                .whenComplete((data, error) -> {
                    Platform.runLater(() -> {
                        if (error != null) {
                            // Handle error case
                            throw new RuntimeException(error);
                        } else if (data != null) {
                            // Handle success case
                            topicsObserver.addAll(data);
                        }
                    });
                });
    }

    public void fetchMessages(int selectedTopicId) {
        this.areMessagesLoading.set(true);
        System.out.println("loading messages");
        this.apiService.fetchMessages(selectedTopicId)
                .whenComplete((data, error) -> {
                    Platform.runLater(() -> {
                        if (error != null) {
                            // Handle error case
                            throw new RuntimeException(error);
                        } else if (data != null) {
                            // Handle success case
                            messageListObserver.addAll(data);
                        }
                        System.out.println("messages loaded");
                        this.areMessagesLoading.set(false);
                    });
                });
    }

    /**
     * Cleans up resources when the application is closing.
     */
    public void shutdown() {
        Topic topic = currentTopicObserver.get();
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
    }
}