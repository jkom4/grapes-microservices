package grapes.microservices.frontendchat.views.components; // Adjust the package name if needed

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;

/**
 * A reusable JavaFX component representing a card
 * containing a title and a description.
 * This class extends VBox to vertically arrange its elements.
 */
public class BubbleMessageFx extends VBox {
    @FXML private HBox messageOrienter;
//    @FXML private VBox messageBubble;
    @FXML private VBox bubbleMessageContainer;
    @FXML private Label bubbleLabel;
    @FXML private Label messageAuthor;
    @FXML private Label messageDate;
    @FXML private HBox messageHeaderContainer;

    // the reason I use ObjectProperty<Boolean> instead of SimpleBooleanProperty, it's because I need null value, so
    //   the observer can notify when we go from null to false, otherwise it won't notify for the cas : false => false
    private final ObjectProperty<Boolean> isMyMessage = new SimpleObjectProperty<>(null);
    public boolean getIsMyText() {return isMyMessage.get();}
    public void setIsMyText(boolean value) {this.isMyMessage.set(value);}
    @Getter @Setter private String text = "";
    @Getter @Setter private String author = "";
    @Getter @Setter private String date = "";

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
        //* The only way to update the component here is by updating isMyMessage
        isMyMessage.addListener(change -> {
            if (isMyMessage.get()) {
                bubbleMessageContainer.getStyleClass().add("my-message");
                messageOrienter.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                messageHeaderContainer.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
                bubbleLabel.setTextAlignment(TextAlignment.RIGHT);
            }
            bubbleLabel.setText(text);
            messageAuthor.setText(author);
            messageDate.setText(date);
        });
    }
}