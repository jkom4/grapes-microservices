package grapes.microservices.frontendchat.viewmodels;


import grapes.microservices.frontendchat.SceneController;
import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;
import grapes.microservices.frontendchat.models.User;
import grapes.microservices.frontendchat.models.services.IGrapesApi;
import grapes.microservices.frontendchat.models.services.IMulticastService;
import grapes.microservices.frontendchat.models.services.IPusherService;
import grapes.microservices.frontendchat.models.shared.UserSession;
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
    private final IPusherService pusherService;
    @Getter
    private SceneController sceneController;

    // Observers
    @Getter // Update topic list when user does research
    private final ObservableList<Topic> topicsObserver = FXCollections.observableArrayList();
    @Getter // Update current topic when user select a topic
    private final ObjectProperty<Topic> currentTopicObserver = new SimpleObjectProperty<>(null);
    @Getter // Update message list when new topic selected or message sent by user
    private final ObservableList<Message> messageListObserver = FXCollections.observableArrayList();
    @Getter // Display or hide messages loading animation when user select a topic
    private final SimpleBooleanProperty areMessagesLoading = new SimpleBooleanProperty(false);
    @Getter // Updated when user send messages, then actions are executed
    private final SimpleStringProperty postedMessageObserver = new SimpleStringProperty("");
    @Getter // Updated when user send messages, then actions are executed
    private ObjectProperty<Message> receivedMessageObserver = new SimpleObjectProperty<>();

    // Constructor
    public ChatViewModel(IMulticastService multicastService, IGrapesApi apiService, IPusherService pusherService, SceneController sceneController) {
        this.multicastService = multicastService;
        this.apiService = apiService;
        this.sceneController = sceneController;
        this.pusherService = pusherService;

        setObserversEvents();
    }

    public void fetchTopics() {
        startMulticastService();
        startPusherService();
        topicsObserver.clear();
        this.apiService.fetchTopics()
                .whenComplete((data, error) -> Platform.runLater(() -> {
                    if (error != null) {
                        // Handle error case
                        throw new RuntimeException(error);
                    } else if (data != null) {
                        // Handle success case
                        topicsObserver.addAll(data);
                    }
                }));
    }

    public void fetchMessages(String selectedTopicId) {
        this.areMessagesLoading.set(true);

        this.apiService.fetchMessages(selectedTopicId)
                .whenComplete((data, error) -> Platform.runLater(() -> {
                    if (error != null) {
                        // Handle error case
                        throw new RuntimeException(error);
                    } else if (data != null) {
                        // Handle success case
                        messageListObserver.addAll(data);
                    }
                    this.areMessagesLoading.set(false);
                }));
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
            multicastService.close();
        } catch (Exception e) {
            System.err.println("Error stopping multicast listener during shutdown: " + e.getMessage());
        }
    }

    // ===============
    // === PRIVATE ===
    // ===============
    private void setObserversEvents() {
        // When user send a message, it's added to the list + multicasted in local + notify the api
        setUserSendMessageObserver();
        // When user join/left a topic
        setJoinLeftTopicObserver();
        // When multicast message received
        setMulticastMessageObserver();
        // When pusher message received
        setPusherMessageObserver();
    }

    private void setUserSendMessageObserver() {

        postedMessageObserver.addListener((observable, oldValue, newValue) -> {
            // when user sends an empty message, nothing happens
            if (newValue.isEmpty()) return;

            // !!! message created in local!
            var topicId = currentTopicObserver.get().id();
            var authUser = UserSession.getINSTANCE().getAuthenticatedUser().get();
            var message = new Message("0", topicId, authUser, newValue, LocalDateTime.now());
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
    }

    private void startMulticastService() {
        try {
            multicastService.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startPusherService() {
        pusherService.connect();
    }

    private void setJoinLeftTopicObserver() {
        currentTopicObserver.addListener((observable, oldTopic, newTopic) -> {
            boolean HAS_OPENED_TOPIC = oldTopic == null && newTopic != null; // if before, topic window was closed
            boolean HAS_CHANGED_TOPIC = oldTopic != null && newTopic != null; // if before, a topic was closed
            boolean HAS_CLOSED_TOPIC = oldTopic != null && newTopic == null; // if user just closed a topic

            try {
                if (HAS_OPENED_TOPIC) {
                    multicastService.joinTopic(newTopic);
                    pusherService.subscribe(newTopic.id());
                }
                if (HAS_CHANGED_TOPIC) {
                    multicastService.leaveTopic(oldTopic);
                    pusherService.unsubscribe(oldTopic.id());
                    multicastService.joinTopic(newTopic);
                    pusherService.subscribe(newTopic.id());
                }
                if (HAS_CLOSED_TOPIC) {
                    multicastService.leaveTopic(oldTopic);
                    pusherService.unsubscribe(oldTopic.id());
                }
            } catch (IOException e) {throw new RuntimeException(e);}
        });
    }

    private void setMulticastMessageObserver() {
        // 1. IF HE RECEIVES HIS OWN MESSAGE, IT'S IGNORED
        // 2. /!\ CURRENTLY I MANAGE THE MESSAGE UNICITY BY THE DATE, to avoid duplicated messages (not the best way)
        // 3. As there is limited number of multicast groups (253), collisions can happen => so i check topic id
        multicastService.getMessageReveiceObserver().addListener((change, oldMessage, newMessage) -> {
            final boolean IS_HIS_OWN_MESSAGE = newMessage.sender().id().equals(UserSession.getINSTANCE().getAuthenticatedUser().get().id());
            final boolean IS_UNIQUE = !isMessageDateInLastX(messageListObserver, newMessage.getDateToString(), 10);
            final boolean IS_MESSAGE_FROM_ANOTHER_TOPIC = !newMessage.topicId().equals(currentTopicObserver.get().id());

            if (IS_HIS_OWN_MESSAGE) {
                System.err.println("[Chat ViewModel] Received your own message (via multicast) => ignored");
                return;
            }
            if (!IS_UNIQUE) {
                System.err.println("[Chat ViewModel] Received duplicated message (via multicast) => ignored");
                return;
            }
            if (IS_MESSAGE_FROM_ANOTHER_TOPIC) {
                System.err.println("[Chat ViewModel] Received a message from another topic (via multicast) => ignored");
                return;
            }

            // This code is executed by a system thread that is not realted to JavaFx, So it will cause Exception
            // => to avoid this problem, We transfert the code below into a JavaFX thread :
            Platform.runLater(() -> {
                    messageListObserver.add(newMessage);
                    System.out.println("[ChatViewModel] Added multicast message to list: " + newMessage); // Optional logging
            });
        });
    }

    private void setPusherMessageObserver() {
        pusherService.setReceivedMessageObserver(receivedMessageObserver);
        receivedMessageObserver.addListener((change, oldMessage, newMessage) -> {
            final boolean IS_UNIQUE = !isMessageDateInLastX(messageListObserver, newMessage.getDateToString(), 10);

            if (!IS_UNIQUE) {
                System.err.println("[Chat ViewModel] Received duplicated message (via Pusher) => ignored");
                return;
            }

            Platform.runLater(() -> {
                    messageListObserver.add(newMessage);
                    System.out.println("[ChatViewModel] Added pusher message to list: " + newMessage); // Optional logging
            });
        });
    }

    public static boolean isMessageDateInLastX(ObservableList<Message> messageList, String date, int count) {
        // Check if the list has fewer than 10 messages
        int size = messageList.size();
        if (size == 0) return false;

        int startIndex = Math.max(0, size - count);
        // Iterate over the last 10 messages
        for (int i = startIndex; i < size; i++) {
            if (messageList.get(i).timestamp().isEqual(date)) {
                return true; // Message ID found
            }
        }
        return false; // Message ID not found
    }

    public ObjectProperty<User> getAuthenticatedUserObserver() {
        return UserSession.getINSTANCE().getAuthenticatedUser();
    }
}