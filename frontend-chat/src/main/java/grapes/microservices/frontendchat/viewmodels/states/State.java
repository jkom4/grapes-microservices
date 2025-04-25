package grapes.microservices.frontendchat.viewmodels.states;

public class State extends Exception {
    public State(String s) {
        super(s);
    }

    public State() {
        super("");
    }
}
