package grapes.microservices.frontendchat.viewmodels;


import grapes.microservices.frontendchat.SceneController;
import grapes.microservices.frontendchat.models.User;
import grapes.microservices.frontendchat.models.services.IGrapesApi;
import grapes.microservices.frontendchat.models.shared.UserSession;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;


/**
 * ViewModel for the Auth View.
 * Acts as an intermediary between the View (FXML) and the Model (Services).
 * Exposes data via JavaFX Properties for binding and handles user actions.
 */
public class AuthViewModel {
    // Services
    private final IGrapesApi apiService;
    @Getter
    private SceneController sceneController;
    @Getter
    private ObjectProperty<Exception> authErrorObserver = new SimpleObjectProperty<>(null);
    @Getter
    private ObjectProperty<User> authenticatedUser = UserSession.getINSTANCE().getAuthenticatedUser();

    // Constructor
    public AuthViewModel(IGrapesApi apiService, SceneController sceneController) {
        this.apiService = apiService;
        this.sceneController = sceneController;
    }

    public void authUser(String token) {
        // Impossible to throw exception in a lambda, so instead, Exceptions are transported in Observables
        this.apiService.authUser(token)
                .whenComplete((data, error) -> Platform.runLater(() -> {
                    if (error != null) {
                        // Handle error case
                        error.printStackTrace();
                        authErrorObserver.set(new Exception(error.getMessage()));
                    } else if (data != null) {
                        // Save user in a singleton
                        UserSession.getINSTANCE().getAuthenticatedUser().set(data);
                    }
                }));
    }
}