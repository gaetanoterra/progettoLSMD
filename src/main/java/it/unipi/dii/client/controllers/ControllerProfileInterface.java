package it.unipi.dii.client.controllers;


import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;

public class ControllerProfileInterface {

    private ServerConnectionManager serverConnectionManager;

    private ObservableList<Post> postObservableList;

    @FXML
    private ListView<Post> myPostsListView;
    @FXML
    private Label displayNameLabel;
    @FXML
    private WebView aboutMeWebView;
    @FXML
    private ImageView profileImageImageView;
    @FXML
    private ListView<User> peopleYouMightKnowListView;
    @FXML
    private ImageView lensImageView;
    @FXML
    private ImageView writePostImageview;
    @FXML
    private ListView yourAnswersListView;
    @FXML
    private ImageView logOutImageView;


    public ControllerProfileInterface() {
        this.serverConnectionManager = ClientInterface.getServerConnectionManager();
        this.postObservableList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize(){
        this.myPostsListView.setItems(this.postObservableList);
        this.myPostsListView.setCellFactory(plv->new ControllerPostBriefViewCell(PageType.PROFILE_INTERFACE));
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
    public void fillProfileInterface(User u){}

    @FXML
    public void logOut(Event event) {
    }

    @FXML
    public void switchToWritePostInterface(Event event) {
    }

    @FXML
    public void switchToPostSearchInterface(Event event) {
    }

}
