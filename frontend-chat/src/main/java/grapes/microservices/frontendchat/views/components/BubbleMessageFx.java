package grapes.microservices.frontendchat.views.components; // Adjust the package name if needed

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

/**
 * A reusable JavaFX component representing a card
 * containing a title and a description.
 * This class extends VBox to vertically arrange its elements.
 */
public class BubbleMessageFx extends VBox {
    @FXML private HBox messageOrienter;
    @FXML private VBox messageBubble;
    @FXML private VBox bubbleMessageContainer;
    @FXML private Label bubbleLabel;

    private final SimpleBooleanProperty isMyMessage = new SimpleBooleanProperty(false);
    public boolean getIsMyText() {return isMyMessage.get();}
    public void setIsMyText(boolean value) {this.isMyMessage.set(value);}
    private final SimpleStringProperty text = new SimpleStringProperty("");
    public String getText() {return text.get();}
    public void setText(String value) {this.text.set(value);}


    public BubbleMessageFx() {
        URL fxmlUrl = getClass().getResource("/grapes/microservices/frontendchat/components/bubble-message-component.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        bindObservers();
    }

    public void bindObservers() {
        isMyMessage.addListener(change -> {
            if (isMyMessage.get()) {
                bubbleMessageContainer.getStyleClass().add("my-message");
                messageOrienter.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                bubbleLabel.setAlignment(Pos.CENTER_RIGHT);
            }
        });
        text.addListener(change -> {
            bubbleLabel.setText(text.get());
        });
    }
}