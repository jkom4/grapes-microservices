package grapes.microservices.frontendchat.views.components;

import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class LoadingFx extends HBox {

    public LoadingFx() {
        Circle circle = new Circle(20);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.MAGENTA);
        circle.setStrokeWidth(5);
        circle.getStrokeDashArray().addAll(10.0, 15.0); // creates a dashed effect for visual appeal

        this.getChildren().add(circle);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(10));

        RotateTransition rotate = new RotateTransition(Duration.seconds(1), circle);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.setInterpolator(javafx.animation.Interpolator.LINEAR);
        rotate.play();
    }
}
