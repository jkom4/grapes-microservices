package grapes.microservices.frontendchat.models.services;

import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;

public interface IHttpAuthService {

    void setRedirectUrlObserver(SimpleStringProperty redirectUrlObserver);

    void setTokenObserver(SimpleStringProperty tokenObserver);

    void start() throws IOException;

    void stop();
}
