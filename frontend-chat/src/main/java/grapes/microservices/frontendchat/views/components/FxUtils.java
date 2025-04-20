package grapes.microservices.frontendchat.views.components;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

public class FxUtils {
    public static void fadeIn(Node node, int duration) {
        // animations
        node.setOpacity(0);
        PauseTransition pause = new PauseTransition(Duration.millis(duration));
        pause.setOnFinished(event -> {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(100), node);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        }); // Task to run after 1 second
        pause.play(); // Start the timer

    }

    public static void scrollToTheBottom(ScrollPane messagesScroller) {
        Timeline timeline = new Timeline();

        // Animate the vvalue to 1.0 (bottom of the scroll pane)
        KeyValue kv = new KeyValue(messagesScroller.vvalueProperty(), 1.0);
        KeyFrame kf = new KeyFrame(Duration.millis(100), kv); // 500ms for smoothness

        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    public static void wait(int duration, EventHandler<ActionEvent> event) {
        PauseTransition pause = new PauseTransition(Duration.seconds(duration)); // Wait for 3 seconds
        pause.setOnFinished(event);
        pause.play();
    }
}
