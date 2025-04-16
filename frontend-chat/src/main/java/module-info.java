module grapes.microservices.frontendchat {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Third-party library requirements
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires static lombok;
    requires java.compiler;
    requires com.google.gson;
    requires java.net.http;
    opens grapes.microservices.frontendchat.models.dto to com.google.gson;

    // Opens the BASE package containing FrontendChat.java to necessary modules
    opens grapes.microservices.frontendchat to javafx.fxml, javafx.graphics;

    // Opens the VIEWS package containing ChatViewController.java specifically to javafx.fxml
    // This allows FXMLLoader to instantiate and inject into your controller.
    opens grapes.microservices.frontendchat.views to javafx.fxml;

    exports grapes.microservices.frontendchat;
    exports grapes.microservices.frontendchat.models;
    exports grapes.microservices.frontendchat.views.components;
    opens grapes.microservices.frontendchat.views.components to javafx.fxml, javafx.graphics;
}