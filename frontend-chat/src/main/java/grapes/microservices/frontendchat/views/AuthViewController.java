package grapes.microservices.frontendchat.views;

import grapes.microservices.frontendchat.SceneController;
import grapes.microservices.frontendchat.models.User;
import grapes.microservices.frontendchat.viewmodels.AuthViewModel;
import grapes.microservices.frontendchat.views.components.LoadingFx;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller for the authentication (auth-view.fxml).
 */
public class AuthViewController implements Initializable {
    // References to Fx components
    @FXML TextField tokenTextfield;
    @FXML LoadingFx loadingFx;
    @FXML Label errorMessageFx;
    @FXML Button authButton;

    // Reference to the ViewModel
    private AuthViewModel viewModel;

    // State
    private SimpleBooleanProperty isLoading = new SimpleBooleanProperty(false);
    private ObjectProperty<Exception> authErrorObserver;
    private ObjectProperty<User> authenticatedUser;
    private SceneController sceneController;

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
    public void setViewModel(AuthViewModel viewModel) {
        this.viewModel = viewModel;
        this.sceneController = viewModel.getSceneController();

        bindViewModel();
    }

    /**
     * Sets up the bindings between the View's UI elements and the ViewModel's properties.
     */
    private void bindViewModel() {
        // TODO add a textfield component
        // TODO add textfied observer
        // TODO add event after token entered
        // TODO in the event, auth api call then save the user in user session

        // === OBSERVERS ===
        authErrorObserver = viewModel.getAuthErrorObserver();
        authenticatedUser = viewModel.getAuthenticatedUser();

        // === Add listeners ===
        // manage loading component visibility
        isLoading.addListener((change, oldValue, newValue) -> loadingFx.setVisible(newValue));
        // manage the token entered after "Enter" pressed
        tokenTextfield.setOnAction(event -> handleSubmit());
        // manage the token entered after Button clicked
        authButton.setOnAction(event -> handleSubmit());
        // manage when user has been authenticated by the api => switch scene
        authenticatedUser.addListener((change, oldValue, newValue) -> {
            sceneController.switchToScene(SceneController.SCENE.CHAT);
            isLoading.set(false);
        });
        // but if instead of authentication we got error, display error
        viewModel.getAuthErrorObserver().addListener(change -> {
            // if error message is not empty, then this message should be displayed
            var errorMessage = authErrorObserver.get().getMessage();
            var isError = !errorMessage.isEmpty();

            if (!isError) return; // If error empty, then there is no error

            isLoading.set(false);
            errorMessageFx.setVisible(true);
            errorMessageFx.setText(errorMessage);
        });
    }

    public void handleSubmit() {
        // Check the token from the API
        isLoading.set(true);
        errorMessageFx.setVisible(false);
        var token = tokenTextfield.getText();
        viewModel.authUser(token);
    }
}