package grapes.microservices.frontendchat.views;

import grapes.microservices.frontendchat.SceneController;
import grapes.microservices.frontendchat.viewmodels.ChatViewModel;
import grapes.microservices.frontendchat.views.components.ChatFx;
import grapes.microservices.frontendchat.views.components.TopicListFx;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller for the main chat view (chat-view.fxml).
 */
public class ChatViewController implements Initializable {
    // References to Fx components
    @FXML public ImageView logoutButton;
    @FXML private TopicListFx topicListFx;
    @FXML private ChatFx chatFx;

    // Reference to the ViewModel
    private ChatViewModel viewModel;

    // State
    private SceneController sceneController;


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
        this.sceneController = viewModel.getSceneController();

        bindViewModel();
    }

    /**
     * Sets up the bindings between the View's UI elements and the ViewModel's properties.
     */
    private void bindViewModel() {
        // === OBSERVERS ===
        var currentTopicObserver = viewModel.getCurrentTopicObserver();
        var messageListObserver = viewModel.getMessageListObserver();
        var areMessagesLoadingObserver = viewModel.getAreMessagesLoading();
        var authenticatedUserObserver = viewModel.getAuthenticatedUserObserver();
        var postedMessageObserver = viewModel.getPostedMessageObserver();
        var topicListObserver = viewModel.getTopicsObserver();

        // === OBSERVERS BINDING TO THE VIEWS ===
        //  This step is important, I pass the observers to the components so they can react to changes
        topicListFx.setTopicListObserver(topicListObserver);
        topicListFx.setSelectedTopicObserver(currentTopicObserver);
        topicListFx.setCurrentUserObserver(authenticatedUserObserver);

        chatFx.setCurrentUserObserver(authenticatedUserObserver);
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

        // Triggered when user authenticate
        authenticatedUserObserver.addListener(change -> {
            if (authenticatedUserObserver.get() == null) return;

            // viewmodel fetch
            viewModel.fetchTopics();
            // In the beginning, update the topic view with fetched data
            topicListFx.updateTopicsContainer(viewModel.getTopicsObserver());
        });

        // Triggered when user press logout button
        logoutButton.setOnMouseClicked(mouseEvent -> {
            authenticatedUserObserver.set(null);
            currentTopicObserver.set(null);
            topicListObserver.clear();
            viewModel.shutdown();
            sceneController.switchToScene(SceneController.SCENE.AUTH);
        });
    }
}