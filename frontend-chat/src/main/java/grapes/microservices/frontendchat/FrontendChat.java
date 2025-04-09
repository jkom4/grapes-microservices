package grapes.microservices.frontendchat;

import grapes.microservices.frontendchat.models.services.IGrapesApi;
import grapes.microservices.frontendchat.models.services.GrapesApi;
import grapes.microservices.frontendchat.models.services.MulticastService;
import grapes.microservices.frontendchat.models.services.IMulticastService;
import grapes.microservices.frontendchat.viewmodels.ChatViewModel;
import grapes.microservices.frontendchat.views.ChatViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;

/**
 * Main application class for the Frontend Chat.
 * Initializes JavaFX, sets up MVVM components, and manages the main stage.
 */
public class FrontendChat extends Application {

    private ChatViewModel viewModel; // Keep a reference to call shutdown()

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Application starting...");

        // 1. Instantiate Services (using Mocks for now)
        IMulticastService multicastService = new MulticastService();
        IGrapesApi apiService = new GrapesApi();

        // 2. Instantiate ViewModel, injecting services
        viewModel = new ChatViewModel(multicastService, apiService);
        System.out.println("ViewModel instantiated.");

        // 3. Load the FXML view
        URL fxmlUrl = getClass().getResource("chat-view.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Parent root = fxmlLoader.load();

        // 4. Get the Controller instance created by the FXMLLoader
        ChatViewController controller = fxmlLoader.getController();

        // 5. Inject the ViewModel into the Controller
        controller.setViewModel(viewModel);

        // 6. Setup the Scene and the Stage
        Scene scene = new Scene(root, 800, 600); // Initial size
        stage.setTitle("JavaFX Multicast Chat (MVVM)");
        stage.setScene(scene);
        System.out.println("Scene and Stage configured.");

        // 7. Handle application close request gracefully
        stage.setOnCloseRequest(this::handleWindowClose);
        System.out.println("Close request handler set.");

        // 8. Show the main window
        stage.show();
        System.out.println("Stage is now showing. Application started successfully.");
    }

    /**
     * Handles the window close event (e.g., clicking the 'X' button).
     * Ensures ViewModel resources are cleaned up.
     * @param event The window event.
     */
    private void handleWindowClose(WindowEvent event) {
        System.out.println("Shutdown requested via window close...");
        if (viewModel != null) {
            try {
                viewModel.shutdown(); // Call the ViewModel's cleanup method
            } catch(Exception e) {
                System.err.println("Error during ViewModel shutdown: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ViewModel was null during shutdown sequence.");
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