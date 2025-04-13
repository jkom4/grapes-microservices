package grapes.microservices.frontendchat.views.components;

import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;
import grapes.microservices.frontendchat.models.User;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatFx extends VBox {
    // FX component id binding
    @FXML private ScrollPane messagesScroller;
    @FXML private VBox messagesContainer;
    @FXML private LoadingFx loadingFx;
    @FXML private TextField messageTextfield;
    @FXML private Label topicText;
    @FXML private ImageView sendMessageButton;
    @FXML private ImageView closeTopicButton;
    @FXML private ImageView refreshMessagesButton;

    // Observers
    @Getter
    private ObjectProperty<Topic> selectedTopic;
    private ObservableList<Message> loadedMessages;
    private ObjectProperty<User> userObserver;
    private SimpleBooleanProperty loadingStatus;
    @Getter
    private SimpleStringProperty currentPostedMessage;

    public ChatFx() {
        URL fxmlUrl = getClass().getResource("/grapes/microservices/frontendchat/components/chat-component.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        bindObservers();
    }

    private void bindObservers() {
        // clear messages (initial examples in the view)
        this.messagesContainer.getChildren().clear();
    }

    public void setSelectedTopicObserver(ObjectProperty<Topic> observer) {
        this.selectedTopic = observer;
        // update label when topic has changed
        selectedTopic.addListener((change, oldTopic, newTopic) -> {
            if (selectedTopic.get() != null) {
                topicText.setText(selectedTopic.get().name());
            }
        });
        // remove current topic when topic closed
        closeTopicButton.setOnMouseClicked(event -> {
            selectedTopic.set(null);
        });
        // refresh all messages
        refreshMessagesButton.setOnMouseClicked(event -> {
            var currentTopic = selectedTopic.get();
            selectedTopic.set(null);
            Platform.runLater(() -> selectedTopic.set(currentTopic));
        });
    }

    public void setMessageListObserver(ObservableList<Message> observer) {
        this.loadedMessages = observer;
        // update message list when messages are loaded
        loadedMessages.addListener((ListChangeListener<Message>) change -> {
            // treat all change notifications
            while (change.next()) {
                // Here, there are 2 cases :
                // 1. the message list is completely different from the previous one => probably from another topic
                // 2. the message list is similar to the previous one, new messages added => from the same topic
                // If it's the case 2, no need to completely refresh the view, just add the missing messages
                if (change.wasAdded()) {
                    AtomicInteger index = new AtomicInteger(change.getAddedSubList().size()); // for fade in effect
                    change.getAddedSubList().forEach(message -> {
                        var bubbleMessage = addMessageToTheList(message);
                        // Add fade in effect (index-- => reversed)
                        EffectUtils.fadeIn(bubbleMessage, index.getAndDecrement() * 20);
                    });
                } else if (change.wasRemoved()) {
                    // if messages were removed from the list, it means that it was cleared, so we empty the view
                    messagesContainer.getChildren().clear();
                }
            }
        });

        // Scroll to the bottom observer
        messagesContainer.heightProperty().addListener((observable, oldValue, newValue) ->
                EffectUtils.scrollToTheBottom(messagesScroller));
    }

    public void setCurrentUserObserver(ObjectProperty<User> authUserObserver) {
        this.userObserver = authUserObserver;
    }

    public void setLoadingStatusObserver(SimpleBooleanProperty observer) {
        this.loadingStatus = observer;

        observer.addListener(change -> {
            // Depending on if messages are loading, it display loading view or messages
            if (loadingStatus.get()) {
                messagesScroller.setManaged(false);
                messagesScroller.setVisible(false);
                loadingFx.setManaged(true);
                loadingFx.setVisible(true);
            } else {
                messagesScroller.setVisible(true);
                messagesScroller.setManaged(true);
                loadingFx.setVisible(false);
                loadingFx.setManaged(false);
            }
        });
    }

    /**
     * Instead of reloading the whole message list view after changes, when just one message is added in the list
     * @param message : message
     * @return generated bubbleMessage, usefull for animation
     */
    public BubbleMessageFx addMessageToTheList(Message message) {
        var bubbleMessage = new BubbleMessageFx();
        bubbleMessage.setText(message.content());
        bubbleMessage.setAuthor(message.sender().username());
        bubbleMessage.setDate(message.getDateToString());
        // if the message belongs to the user, then the message bubble is aligned to the left with another color
        var isMyMessage = message.sender().id() == userObserver.get().id();
        bubbleMessage.setIsMyText(isMyMessage);
        messagesContainer.getChildren().add(bubbleMessage);

        return bubbleMessage;
    }

    /**
     * Each time the user press enter to post a message, this observer notify its subscribers
     * @param observer : post message field
     */
    public void setOnMessagePostObserver(SimpleStringProperty observer) {
        this.currentPostedMessage = observer;

        sendMessageButton.setOnMouseClicked(event -> {
            handleSendMessage();
        });
        messageTextfield.setOnAction(event -> handleSendMessage());
    }

    // this function is called by 2 events : "On textfield Enter pressed" and "On send button clicked"
    private void handleSendMessage() {
        String message = messageTextfield.getText();
        // The reason why there are 2 "set" it's because the observer notify only when the old and new value are
        // different, so I firstly set it to "" (empty messages are not posted), then I erase it with "message" value
        currentPostedMessage.set("");
        currentPostedMessage.set(message); // the observer notify its listeners
        messageTextfield.clear();
    }
}
