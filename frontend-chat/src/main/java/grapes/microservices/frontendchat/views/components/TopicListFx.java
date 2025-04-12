package grapes.microservices.frontendchat.views.components;

import grapes.microservices.frontendchat.models.Topic;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TopicListFx extends VBox {
    // References to Fx components
    @FXML private VBox topicsContainer;
    @FXML private ScrollPane topicScroller;
    @FXML private ListView<Topic> topicListView;
    @FXML private TextField topicTextfield;

    // Observers
    private ObjectProperty<Topic> selectedTopic;
    private List<Topic> topics = new ArrayList<>();

    public TopicListFx() {
        URL fxmlUrl = getClass().getResource("/grapes/microservices/frontendchat/components/topics-component.fxml");
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

    private void bindObservers() {
        // update the list when filter is modified and user press "Enter"
        topicTextfield.setOnAction(event -> {
            updateTopicsContainer(this.topics);
        });
    }

    public void updateTopicsContainer(List<Topic> currentTopics) {
        this.topics = currentTopics;
        // Clear existing children from the VBox
        topicsContainer.getChildren().clear();

        if (currentTopics.isEmpty()) {
            topicsContainer.getChildren().add(new LoadingFx());
            return;
        }

        // Create and add new cards for each topic in the current list
        int index = 0;
        for (Topic topic : currentTopics) {
            // check if it's corresponding to search filter, if not => excluded from the list
            // by default, the filter value is empty, empty string is always contained in a topic name => always found
            boolean topicFound = topic.name().toLowerCase().contains(topicTextfield.getText().toLowerCase());
            if (!topicFound) {
                continue;
            }

            // create a topic card to add on the topic list
            CardFx card = new CardFx();
            card.getStyleClass().add("topic-card"); // Add a style class for CSS
            card.setTitleFx(topic.name());
            card.setDescriptionFx(topic.lastMessage());
            card.setOnMouseClicked(mouseEvent -> {
                selectedTopic.set(topic);
            });
            // Add the custom component to the VBox
            topicsContainer.getChildren().add(card);
            // Add fade in effect
            EffectUtils.fadeIn(card, index++ * 20);
        }
    }

    public void setTopicListObserver(ObservableList<Topic> topics) {
        topics.addListener((ListChangeListener<Topic>) change -> this.updateTopicsContainer(topics));
    }

    public void setSelectedTopicObserver(ObjectProperty<Topic> selectedTopic) {
        this.selectedTopic = selectedTopic;
    }
}
