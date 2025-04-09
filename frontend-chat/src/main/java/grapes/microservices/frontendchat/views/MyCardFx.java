package grapes.microservices.frontendchat.views; // Adjust the package name if needed

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A reusable JavaFX component representing a card
 * containing a title and a description.
 * This class extends VBox to vertically arrange its elements.
 */
public class MyCardFx extends VBox {

    // The Text objects to display the title and description
    private final Text titleTextNode;
    private final Text descriptionTextNode;

    /**
     * Constructor to create a new card.
     *
     * @param title The title text to display on the card.
     * @param description The description text to display on the card.
     */
    public MyCardFx(String title, String description) {
        super(); // Call the constructor of the parent class (VBox)

        // --- Configure Text objects ---
        titleTextNode = new Text(title);
        // Set the font (name, weight, size) for the title
        titleTextNode.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        // Set the text color for the title
        titleTextNode.setFill(Color.DARKSLATEBLUE);

        descriptionTextNode = new Text(description);
        // Set the font for the description
        descriptionTextNode.setFont(Font.font("Arial", 12));
        // Set the text color for the description
        descriptionTextNode.setFill(Color.DIMGRAY);
        // Default wrapping width (can be adjusted if needed).
        // Adjust this value based on the desired width for your cards.
        descriptionTextNode.setWrappingWidth(300);

        // --- Configure the VBox (the card itself) ---
        // Vertical spacing between child elements (title and description)
        this.setSpacing(10);
        // Inner padding between the card's borders and its content
        this.setPadding(new Insets(15));
        // Add the Text objects (title and description) as children of the VBox
        this.getChildren().addAll(titleTextNode, descriptionTextNode);

        // --- Apply default CSS styling to the card ---
        applyDefaultStyle();
    }

    /**
     * Applies the default CSS style to the card (background, border, shadow).
     */
    private void applyDefaultStyle() {
        this.setStyle(
                "-fx-background-color: white;" +           // White background color
                        "-fx-border-color: #cccccc;" +            // Border color (light gray)
                        "-fx-border-width: 1px;" +               // Border thickness
                        "-fx-border-radius: 8px;" +              // Radius for rounded border corners
                        "-fx-background-radius: 8px;" +          // Radius for rounded background corners (matches border)
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);" // Light drop shadow effect
        );
    }

    // --- Public methods to modify the card after creation ---

    /**
     * Allows changing the title text after the card is created.
     * @param title The new title text.
     */
    public void setTitle(String title) {
        this.titleTextNode.setText(title);
    }

    /**
     * Allows changing the description text after the card is created.
     * @param description The new description text.
     */
    public void setDescription(String description) {
        this.descriptionTextNode.setText(description);
    }

    /**
     * Sets the maximum width (in pixels) before the description text
     * automatically wraps to the next line.
     * @param width The desired wrapping width.
     */
    public void setDescriptionWrappingWidth(double width) {
        this.descriptionTextNode.setWrappingWidth(width);
    }

    /**
     * Gets the Text object used for the title.
     * Useful if you want to apply more advanced modifications (e.g., event handlers).
     * @return The Text object for the title.
     */
    public Text getTitleTextNode() {
        return titleTextNode;
    }

    /**
     * Gets the Text object used for the description.
     * Useful if you want to apply more advanced modifications.
     * @return The Text object for the description.
     */
    public Text getDescriptionTextNode() {
        return descriptionTextNode;
    }

    // You could add more methods here for finer customization:
    // - Change fonts (setTitleFont, setDescriptionFont)
    // - Change colors (setTitleColor, setDescriptionColor, setCardBackgroundColor)
    // - Change the overall style (setCardStyle)
}