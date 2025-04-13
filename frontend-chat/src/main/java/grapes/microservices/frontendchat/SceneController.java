package grapes.microservices.frontendchat;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class SceneController {
    private final Stage stage;
    private final Map<SCENE, Scene> scenes = new HashMap<>();

    public SceneController(Stage stage) {
        this.stage = stage;
    }

    public void switchToScene(SCENE scene) {
        try {
            stage.setScene(scenes.get(scene));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerScene(SCENE scene, Scene authScene) {
        scenes.put(scene, authScene);
    }

    public enum SCENE {
        AUTH,
        CHAT
    }
}
