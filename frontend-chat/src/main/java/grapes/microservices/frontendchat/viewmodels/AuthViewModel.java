package grapes.microservices.frontendchat.viewmodels;


import grapes.microservices.frontendchat.SceneController;
import grapes.microservices.frontendchat.models.User;
import grapes.microservices.frontendchat.models.exceptions.MyApiException;
import grapes.microservices.frontendchat.models.services.IHttpAuthService;
import grapes.microservices.frontendchat.viewmodels.states.*;
import grapes.microservices.frontendchat.models.services.IGrapesApi;
import grapes.microservices.frontendchat.models.shared.UserSession;
import grapes.microservices.frontendchat.viewmodels.states.State;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

import java.io.IOException;


/**
 * ViewModel for the Auth View.
 * Acts as an intermediary between the View (FXML) and the Model (Services).
 * Exposes data via JavaFX Properties for binding and handles user actions.
 */
public class AuthViewModel {
    // Services
    private final IGrapesApi apiService;
    private final IHttpAuthService httpAuthService;
    @Getter
    private final SceneController sceneController;
    @Getter
    private final ObjectProperty<State> stateObserver = new SimpleObjectProperty<>(null);
    @Getter
    private final ObjectProperty<User> authenticatedUser = UserSession.getINSTANCE().getAuthenticatedUser();
    @Getter
    private final SimpleStringProperty redirectUrlObserver = new SimpleStringProperty();
    @Getter
    private final SimpleStringProperty tokenObserver =  new SimpleStringProperty();

    // Constructor
    public AuthViewModel(IGrapesApi apiService, SceneController sceneController, IHttpAuthService httpAuthService) {
        this.apiService = apiService;
        this.httpAuthService = httpAuthService;
        this.sceneController = sceneController;

        httpAuthService.setRedirectUrlObserver(redirectUrlObserver);
        setStateObserver();
        setAuthTokenCapturedObserver();
    }

    public void getUser() {
        stateObserver.set(new OnLoading());

        // Impossible to throw exception in a lambda, so instead, Exceptions are transported in Observables
        this.apiService.getUser()
                .whenComplete((data, error) -> Platform.runLater(() -> {
                    if (error != null) {
                        // Handle error case, as my exception is wrapped into another, I use error.getCause() to get it
                        if (error.getCause() instanceof MyApiException) {
                            stateObserver.set(new OnFail(error.getCause().getMessage()));
                        } else {
                            // Handle other error case
                            stateObserver.set(new OnFail(error.getMessage()));
                            error.printStackTrace();
                        }
                    } else if (data != null) {
                        // Save user in a singleton
                        UserSession.getINSTANCE().getAuthenticatedUser().set(data);
                        // error = null => success
                        stateObserver.set(new OnSuccess());
                    }
                }));
    }

    // ===============
    // === PRIVATE ===
    // ===============
    private void setStateObserver() {
        stateObserver.addListener((obs, oldState, newState) -> {
            if (newState instanceof OnAuth) {
                try {
                    httpAuthService.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void setAuthTokenCapturedObserver() {
        httpAuthService.setTokenObserver(tokenObserver);
        // When user send a message, it's added to the list + multicasted in local + notify the api
        tokenObserver.addListener((observable, oldToken, newToken) -> {
            Platform.runLater(() -> {
                UserSession.setToken(newToken);
                getUser();
                httpAuthService.stop();
            });
        });
    }
}
