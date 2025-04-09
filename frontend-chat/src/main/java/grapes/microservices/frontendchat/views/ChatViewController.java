package grapes.microservices.frontendchat.views;

import grapes.microservices.frontendchat.models.Topic;
import grapes.microservices.frontendchat.viewmodels.ChatViewModel;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * FXML Controller for the main chat view (chat-view.fxml).
 * Handles UI element setup and delegates user actions to the ViewModel.
 */
public class ChatViewController implements Initializable {

    // --- FXML Injected Elements ---
    @FXML private VBox topicContainer;
    @FXML private ListView<Topic> topicListView;
    // Reference to the ViewModel
    private ChatViewModel viewModel;

    /**
     * Called by the FXMLLoader after FXML elements are injected.
     * Used for initialization that doesn't depend on the ViewModel yet.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    /**
     * Called by the application's main class to provide the ViewModel instance.
     * @param viewModel The ChatViewModel for this view.
     */
    public void setViewModel(ChatViewModel viewModel) {
        this.viewModel = viewModel;
        bindViewModel(); // Setup bindings once the ViewModel is available
    }

    /**
     * Sets up the bindings between the View's UI elements and the ViewModel's properties.
     */
    private void bindViewModel() {
        topicContainer.setSpacing(15);
        // Adds padding around the entire list of cards.
        topicContainer.setPadding(new Insets(10));
        // Sets a background color for the area where cards are displayed (visible if the ScrollPane is larger).

        viewModel.fetchTopics();
        // Loop to create and add multiple cards to the list.
        for (Topic topic : viewModel.getTopics()) {
            // Create a new instance of our custom component.
            MyCardFx card = new MyCardFx(topic.name(), topic.lastMessage());
            card.getStyleClass().add("cards");

            // Add the card (which is a VBox) as a child of the main container.
            topicContainer.getChildren().add(card);
        }
        ObservableList<Topic> topicsFromViewModel = viewModel.getTopics();
        topicsFromViewModel.addListener((ListChangeListener<Topic>) change -> {
            System.out.println("CONTROLLER: Detected change in ViewModel's topic list!");
            // Update the VBox content whenever the list changes
            updateTopicContainer(topicsFromViewModel); // Pass the current list state
        });
        // Create a ScrollPane to allow scrolling if the list of cards exceeds the window height.
        ScrollPane scrollPane = new ScrollPane();
        // Set the card container as the content of the ScrollPane.
        scrollPane.setContent(topicContainer);
        // Ensure the content (cardListContainer) stretches to fill the width of the ScrollPane.
        // Prevents an unnecessary horizontal scroll bar if cards are narrower than the ScrollPane.
        scrollPane.setFitToWidth(true);
        // Make the ScrollPane's own background transparent to see the cardListContainer's background.
//        scrollPane.setStyle("-fx-background-color: transparent;");
        // Remove the default border/padding of the ScrollPane for better visual integration.
        scrollPane.setStyle("-fx-background-insets: 0; -fx-padding: 0;");
    }

    private void updateTopicContainer(List<Topic> currentTopics) {
        System.out.println("CONTROLLER: Updating VBox children. Current topic count: " + currentTopics.size());

        // Clear existing children from the VBox
        topicContainer.getChildren().clear();

        // Create and add new cards for each topic in the current list
        for (Topic topic : currentTopics) {
            MyCardFx card = new MyCardFx(topic.name(), topic.lastMessage());
            card.getStyleClass().add("topic-card"); // Add a style class for CSS

            // Add the custom component to the VBox
            topicContainer.getChildren().add(card);
        }
    }

    /**
     * FXML Action Handler for the Send Button. Delegates the action to the ViewModel.
     */
    @FXML
    private void handleSendMessage() {
//        if(viewModel != null) {
//            viewModel.sendMessage();
//            // Optional: Auto-scroll to the last message after sending
//            if (!messageListView.getItems().isEmpty()) {
//                messageListView.scrollTo(messageListView.getItems().size() - 1);
//            }
//            messageInputTextField.requestFocus(); // Keep focus on input
//        } else {
//            System.err.println("Send action ignored: ViewModel is null.");
//        }
    }
}