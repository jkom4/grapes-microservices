package grapes.microservices.frontendchat.views;

import grapes.microservices.frontendchat.AppEnv;
import grapes.microservices.frontendchat.SceneController;
import grapes.microservices.frontendchat.models.shared.UserSession;
import grapes.microservices.frontendchat.viewmodels.states.*;
import grapes.microservices.frontendchat.viewmodels.AuthViewModel;
import grapes.microservices.frontendchat.views.components.FxUtils;
import grapes.microservices.frontendchat.views.components.LoadingFx;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller for the authentication (auth-view.fxml).
 */
public class AuthViewController implements Initializable {
    // References to Fx components
    @FXML LoadingFx loadingFx;
    @FXML Label errorMessageFx;
    @FXML Button authButton;
    @FXML WebView authWebView;
    @FXML VBox authContainer;
    @FXML VBox authContainerRoot;
    @FXML ImageView successImage;
    @FXML ImageView errorImage;

    // Reference to the ViewModel
    private AuthViewModel viewModel;

    // State
    private ObjectProperty<State> stateObserver;
    private SceneController sceneController;

    /**
     * Called by the FXMLLoader after FXML elements are injected.
     * Used for initialization that doesn't depend on the ViewModel yet.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        authWebView.prefHeightProperty().bind(authContainerRoot.heightProperty());
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
        // === OBSERVERS ===
        stateObserver = viewModel.getStateObserver();
        // === Add listeners ===

        // manage the token entered after Button clicked
        authButton.setOnAction(event -> handleAuth());

        // Manage the state of the view
        stateObserver.addListener((change, old, error) -> {
            // determine the error type to decide the view behavior
           try {
               // reset state
               resetState();
               // Throw the exception in order to use try/catch feature instead of instanceOf
               System.out.println(error.toString());
               throw error;
           } catch (OnReset e) {
               // Nothing happens because "resetState()" already reset the view state
           } catch (OnAuth e) {
               show(authWebView);
               hide(authContainer);
               FxUtils.wait(100, event -> {
                   authWebView.getEngine().load(AppEnv.AUTH_SERVICE_URL.get());
               });
           } catch (OnLoading e) {
               show(loadingFx);
           } catch (OnSuccess e) {
               show(successImage);
               hide(authButton);
               FxUtils.wait(1000, event -> {
                   sceneController.switchToScene(SceneController.SCENE.CHAT);
                   stateObserver.set(new OnReset());
               });
               // If we need to have a specific view depending on exception type, we can subdivide the Exception
               // In my case, the MyApiConnectionException and Exception gives the same behaviour.
           } catch (Exception e) {
               show(errorMessageFx);
               show(errorImage);
               errorMessageFx.setText(e.getMessage());
           }
        });

        // Listen to WebView when user login, then get the token
        // Add a listener to track the URL changes
        authWebView.getEngine().locationProperty().addListener((obs, oldUrl, newUrl) -> {
            // Check if the new URL contains the token or required value
            if (newUrl.contains("q=")) {
                String token = extractToken(newUrl);
                UserSession.setToken(token);
                viewModel.getUser();
            }
        });

        // Manage the WebView error if it cannot display the website
        authWebView.getEngine().getLoadWorker().exceptionProperty().addListener((obs, oldErr, newErr) -> {
            // Check if the new URL contains the token or required value
            if (newErr == null) return;
            stateObserver.set(new OnFail(newErr.getMessage()));
        });
    }

    private String extractToken(String url) {
        String[] parts = url.split("q=");
        return parts.length > 1 ? parts[1].split("&")[0] : null;
    }

    public void handleAuth() {
        // Check the token from the API
        stateObserver.setValue(new OnAuth());
    }

    private void resetState() {
        hide(authWebView);
        show(authContainer);
        show(authButton);
        hide(errorMessageFx);
        hide(successImage);
        hide(errorImage);
        hide(loadingFx);
    }

    private static void hide(Node node) {
        node.setVisible(false);
        node.setManaged(false);
    }

    private static void show(Node node) {
        node.setVisible(true);
        node.setManaged(true);
    }
}