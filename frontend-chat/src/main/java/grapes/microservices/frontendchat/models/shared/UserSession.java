package grapes.microservices.frontendchat.models.shared;

import grapes.microservices.frontendchat.models.User;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;

public class UserSession {
    @Getter
    @Setter
    private ObjectProperty<User> authenticatedUser = new SimpleObjectProperty<>(null);

    @Getter
    @Setter
    private static String token;

    @Getter
    private static final UserSession INSTANCE = new UserSession();
}
