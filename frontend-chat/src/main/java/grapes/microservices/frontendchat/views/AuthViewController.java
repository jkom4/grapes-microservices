package grapes.microservices.frontendchat.views;

import grapes.microservices.frontendchat.AppEnv;
import grapes.microservices.frontendchat.SceneController;
import grapes.microservices.frontendchat.models.shared.UserSession;
import grapes.microservices.frontendchat.viewmodels.states.*;
import grapes.microservices.frontendchat.viewmodels.AuthViewModel;
import grapes.microservices.frontendchat.views.components.FxUtils;
import grapes.microservices.frontendchat.views.components.LoadingFx;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import lombok.Getter;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller for the authentication (auth-view.fxml).
 */
public class AuthViewController implements Initializable {
    // References to Fx components
    @FXML LoadingFx loadingFx;
    @FXML Label infoMessageFx;
    @FXML Label errorMessageFx;
    @FXML Button authButton;
    @FXML VBox authContainer;
    @FXML VBox authContainerRoot;
    @FXML ImageView successImage;
    @FXML ImageView errorImage;

    // Reference to the ViewModel
    private AuthViewModel viewModel;

    // State
    private ObjectProperty<State> stateObserver;
    private SceneController sceneController;

    // observables
    private SimpleStringProperty redirectUrlObserver = new SimpleStringProperty();
    private SimpleStringProperty tokenObserver =  new SimpleStringProperty();

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
        // === OBSERVERS ===
        stateObserver = viewModel.getStateObserver();
        redirectUrlObserver = viewModel.getRedirectUrlObserver();
        tokenObserver = viewModel.getTokenObserver();
        // === Add listeners ===

        // manage auth token received via http
        redirectUrlObserver.addListener((observable, oldUrl, newUrl) -> {
            Platform.runLater(() -> FxUtils.redirectToUrl(newUrl));
        });

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
               hide(authButton);
               show(loadingFx);
               infoMessageFx.setText("A new page was opened. You must login there in order to be authenticated.");
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
    }

    public void handleAuth() {
        // Check the token from the API
        stateObserver.setValue(new OnAuth());
    }

    private void resetState() {
        show(authContainer);
        show(authButton);
        hide(errorMessageFx);
        hide(successImage);
        hide(errorImage);
        hide(loadingFx);
        infoMessageFx.setText("");
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