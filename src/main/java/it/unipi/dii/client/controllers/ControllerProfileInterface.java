package it.unipi.dii.client.controllers;


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
public class ControllerProfileInterface {

    private ServerConnectionManager serverConnectionManager;

    private ObservableList<Post> postObservableList;

    @FXML
    private ListView<Post> myPostsListView;
    @FXML
    private Button button_logout;
    @FXML
    private Label displayNameLabel;
    @FXML
    private WebView aboutMeWebView;
    @FXML
    private Button button_browse;
    @FXML
    private ImageView profileImageImageView;
    @FXML
    private Button button_write;
    @FXML
    private ListView<User> peopleYouMightKnowListView;


    public ControllerProfileInterface() {
        this.serverConnectionManager = ClientInterface.getServerConnectionManager();
        this.postObservableList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize(){
        this.myPostsListView.setItems(this.postObservableList);
        this.myPostsListView.setCellFactory(plv->new ControllerPostBriefViewCell());
    }

    @FXML
    private void eventButtonBrowse(ActionEvent actionEvent) {

    }

    @FXML
    private void eventButtonWrite(ActionEvent actionEvent) {

    }

    @FXML
    private void eventButtonLogout(ActionEvent actionEvent) {
    }
    public void fillProfileInterface(User u){

    }
}
