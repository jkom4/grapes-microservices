package grapes.microservices.frontendchat.views;

import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.viewmodels.ChatViewModel;
import grapes.microservices.frontendchat.views.components.ChatFx;
import grapes.microservices.frontendchat.views.components.TopicListFx;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * FXML Controller for the main chat view (chat-view.fxml).
 * Handles UI element setup and delegates user actions to the ViewModel.
 */
public class ChatViewController implements Initializable {

    // Reference to the ViewModel
    private ChatViewModel viewModel;

    // References to Fx components
    @FXML private TopicListFx topicListFx;
    @FXML private ChatFx chatFx;

    /**
     * Called by the FXMLLoader after FXML elements are injected.
     * Used for initialization that doesn't depend on the ViewModel yet.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Because there is no selected topic in the beginning, the message view is hidden
        chatFx.setVisible(false);
    }

    /**
     * Called by the application's main class to provide the ViewModel instance.
     * @param viewModel The ChatViewModel for this view.
     */
    public void setViewModel(ChatViewModel viewModel) {
        this.viewModel = viewModel;

        // viewmodel fetch
        viewModel.fetchTopics();
        viewModel.authUser("");

        bindViewModel();

        topicListFx.updateTopicsContainer(viewModel.getTopicsObserver());
    }

    /**
     * Sets up the bindings between the View's UI elements and the ViewModel's properties.
     */
    private void bindViewModel() {
        // === OBSERVERS ===
        var currentTopicObserver = viewModel.getCurrentTopicObserver();
        var messageListObserver = viewModel.getMessageListObserver();
        var areMessagesLoadingObserver = viewModel.getAreMessagesLoading();
        var currentUserObserver = viewModel.getCurrentUserObserver();
        var postedMessageObserver = viewModel.getPostedMessageObserver();

        // === OBSERVERS BINDING TO THE VIEWS ===
        //  This step is important, I pass the observers to the components so they can react to changes
        topicListFx.setTopicListObserver(viewModel.getTopicsObserver());
        topicListFx.setSelectedTopicObserver(currentTopicObserver);

        chatFx.setCurrentUserObserver(currentUserObserver);
        chatFx.setSelectedTopicObserver(currentTopicObserver);
        chatFx.setMessageListObserver(messageListObserver);
        chatFx.setLoadingStatusObserver(areMessagesLoadingObserver);
        chatFx.setOnMessagePostObserver(postedMessageObserver);

        // === EVENTS ===
        //  When Topic is selected/deselected, the Chat view is visible + update its selectedTopic value + load messages
        currentTopicObserver.addListener(observable -> {
            // The reason why this variable is created again it's because we are in another context => up-to-date value
            boolean aTopicIsSelected = currentTopicObserver.get() != null;
            chatFx.setVisible(aTopicIsSelected);
            if (aTopicIsSelected) {
                messageListObserver.clear();
                viewModel.fetchMessages(currentTopicObserver.get().id());
            }
        });
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