package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

public class ControllerFullPostInterface {

    @FXML
    private Text authorText;

    @FXML
    private Label titleLabel;

    @FXML
    private WebView questionWebView;

    @FXML
    private ListView<Answer> answersListView;

    private ObservableList<Answer> answerObservableList;
    private ServerConnectionManager serverConnectionManager;


    public ControllerFullPostInterface() {
        this.answerObservableList = FXCollections.observableArrayList();
        this.serverConnectionManager = ClientInterface.getServerConnectionManager();
    }

    @FXML
    private void initialize(){
        answersListView.setItems(this.answerObservableList);
        answersListView.setCellFactory(alv->new ControllerAnswerCell());
    }

    public void resetInterface() {
        titleLabel.setText("The Title");
        authorText.setText("Pippo");
        questionWebView.getEngine().loadContent("<h1> EMPTY BODY</h1>", "text/html");
        answerObservableList.removeAll();
    }

    public void fillInterface(Post post) {
        System.out.println(post);
        titleLabel.setText(post.getTitle());
        authorText.setText(post.getOwnerUserId());
        questionWebView.getEngine().loadContent(post.getBody(), "text/html");
        answerObservableList.addAll(post.getAnswers());
    }

}
