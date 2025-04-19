package grapes.microservices.frontendchat.views.components; // Adjust the package name if needed

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

/**
 * A reusable JavaFX component representing a card
 * containing a title and a description.
 * This class extends VBox to vertically arrange its elements.
 */
public class CardFx extends VBox {

    // SimpleStringProperty = observe the changes of the title => to update component using it
    private final StringProperty title = new SimpleStringProperty(this, "title", "Default topic"); // (bean, name, initialValue)
    public final String getTitleFx() {return title.get();}
    public final void setTitleFx(String value) {
        title.set(value);}

    // observe the changes of the the description => to update component using it
    private final StringProperty description = new SimpleStringProperty(this, "description", "Default description"); // (bean, name, initialValue)
    public final String getDescriptionFx() {return description.get();}
    public final void setDescriptionFx(String value) {
        description.set(value);}

    // JavaFX components
    @FXML private Text titleFx;
    @FXML private Text descriptionFx;

    public CardFx() {
        URL fxmlUrl = getClass().getResource("/grapes/microservices/frontendchat/components/card-component.fxml");
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Fx initialization
        this.setTitleFx(title.getValue());
        this.setDescriptionFx(description.getValue());
        bindObservers();
    }

    public void bindObservers() {
        title.addListener((change) -> titleFx.setText(title.get()));
        description.addListener((change) -> descriptionFx.setText(description.get()));
    }
}