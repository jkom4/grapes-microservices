package grapes.microservices.frontendchat;

import grapes.microservices.frontendchat.models.services.IGrapesApi;
import grapes.microservices.frontendchat.models.services.GrapesApi;
import grapes.microservices.frontendchat.models.services.MulticastService;
import grapes.microservices.frontendchat.models.services.IMulticastService;
import grapes.microservices.frontendchat.viewmodels.AuthViewModel;
import grapes.microservices.frontendchat.viewmodels.ChatViewModel;
import grapes.microservices.frontendchat.views.AuthViewController;
import grapes.microservices.frontendchat.views.ChatViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * Main application class for the Frontend Chat.
 * Initializes JavaFX, sets up MVVM components, and manages the main stage.
 */
public class FrontendChat extends Application {

    // Keep a reference to call shutdown()
    private ChatViewModel chatVM;
    private AuthViewModel authVM;

    private final int MULTICAST_PORT = 8000;

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("[Application] Application starting");
        // 0. Instantiate Scene controller
        var sceneController = new SceneController(stage);

        // 1. Instantiate Services (using Mocks for now)
        IMulticastService multicastService = new MulticastService(MULTICAST_PORT);
        IGrapesApi apiService = new GrapesApi();

        // 2. Instantiate ViewModels, injecting services
        authVM = new AuthViewModel(apiService, sceneController);
        chatVM = new ChatViewModel(multicastService, apiService, sceneController);

        // 3. Load the FXML view
        //      a. Auth View
        FXMLLoader authFxmlLoader = new FXMLLoader(getClass().getResource("auth-view.fxml"));
        authFxmlLoader.setClassLoader(getClass().getClassLoader());
        Parent authRoot = authFxmlLoader.load();

        //      b. Chat View
        FXMLLoader chatFxmlLoader = new FXMLLoader(getClass().getResource("chat-view.fxml"));
        chatFxmlLoader.setClassLoader(getClass().getClassLoader());
        Parent chatRoot = chatFxmlLoader.load();

        // 4. Get the Controller instances created by the FXMLLoader
        AuthViewController authController = authFxmlLoader.getController();
        ChatViewController chatController = chatFxmlLoader.getController();

        // 5. Inject the ViewModel into the Controller
        authController.setViewModel(authVM);
        chatController.setViewModel(chatVM);

        // 6. Setup the Scene and the Stage
        Scene authScene = new Scene(authRoot, 800, 600);
        Scene chatScene = new Scene(chatRoot, 800, 600);
        sceneController.registerScene(SceneController.SCENE.AUTH, authScene);
        sceneController.registerScene(SceneController.SCENE.CHAT, chatScene);

        stage.setTitle("Grapes Support");
        stage.setScene(authScene);
        stage.setMinWidth(800);
        stage.setMinHeight(500);

        // 7. Handle application close request gracefully
        stage.setOnCloseRequest(this::handleWindowClose);

        // 8. Show the main window
        stage.show();
    }

    /**
     * Handles the window close event (e.g., clicking the 'X' button).
     * Ensures ViewModel resources are cleaned up.
     * @param event The window event.
     */
    private void handleWindowClose(WindowEvent event) {
        System.out.println("Shutdown requested via window close...");
        if (chatVM != null) {
            try {
                chatVM.shutdown(); // Call the ViewModel's cleanup method
            } catch(Exception e) {
                System.err.println("Error during ChatViewModel shutdown: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ChatViewModel was null during shutdown sequence.");
        }
        // Platform.exit() is usually sufficient for JavaFX apps.
        // System.exit(0) is a more forceful way to ensure all threads terminate,
        // useful if non-daemon threads (like maybe in mock services) are hanging.
        Platform.exit();
        System.exit(0);
    }


    /**
     * The main entry point of the application. Launches the JavaFX runtime.
     * @param args Command line arguments (not used in this basic setup).
     */
    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }
}