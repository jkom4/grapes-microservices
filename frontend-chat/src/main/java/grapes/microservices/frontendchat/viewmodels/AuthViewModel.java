package grapes.microservices.frontendchat.viewmodels;


import grapes.microservices.frontendchat.SceneController;
import grapes.microservices.frontendchat.models.User;
import grapes.microservices.frontendchat.models.exceptions.MyApiException;
import grapes.microservices.frontendchat.viewmodels.states.OnFail;
import grapes.microservices.frontendchat.viewmodels.states.OnLoading;
import grapes.microservices.frontendchat.viewmodels.states.OnSuccess;
import grapes.microservices.frontendchat.models.services.IGrapesApi;
import grapes.microservices.frontendchat.models.shared.UserSession;
import grapes.microservices.frontendchat.viewmodels.states.State;
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
    private ObjectProperty<State> stateObserver = new SimpleObjectProperty<>(null);
    @Getter
    private ObjectProperty<User> authenticatedUser = UserSession.getINSTANCE().getAuthenticatedUser();

    // Constructor
    public AuthViewModel(IGrapesApi apiService, SceneController sceneController) {
        this.apiService = apiService;
        this.sceneController = sceneController;
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
}