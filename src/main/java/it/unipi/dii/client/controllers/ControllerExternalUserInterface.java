package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.Opcode;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;

//classe preposta a gestire l'interfaccia del profilo utente
public class ControllerExternalUserInterface {

    private ServerConnectionManager serverConnectionManager;
    private ObservableList<Post> postObservableList;
    private PageType lastPageVisited;

    @FXML
    private ListView<Post> myPostsListView;
    @FXML
    private Button button_back;
    @FXML
    private Label displayNameLabel;
    @FXML
    private WebView aboutMeWebView;
    @FXML
    private ImageView profileImageImageView;


    public ControllerExternalUserInterface() {
        this.serverConnectionManager = ClientInterface.getServerConnectionManager();
        this.postObservableList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize(){
        this.myPostsListView.setItems(this.postObservableList);
        this.myPostsListView.setCellFactory(plv->new ControllerPostBriefViewCell());
    }

    @FXML
    private void eventButtonBack(ActionEvent actionEvent) {
        ClientInterface.switchScene(lastPageVisited);
    }
}
