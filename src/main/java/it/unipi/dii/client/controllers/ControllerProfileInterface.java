package it.unipi.dii.client.controllers;


import it.unipi.dii.Libraries.Messages.MessageLogOut;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;

import java.io.IOException;

import static it.unipi.dii.client.ClientInterface.DEFAULT_USERNAME;


public class ControllerProfileInterface {

    private ServerConnectionManager serverConnectionManager;

    @FXML
    private ListView<Post> myPostsListView;
    private ObservableList<Post> postObservableList;
    @FXML
    private Label displayNameLabel;
    @FXML
    private WebView aboutMeWebView;
    @FXML
    private ImageView profileImageImageView;
    @FXML
    private ListView<User> peopleYouMightKnowListView;
    private ObservableList<User> userObservableList;
    @FXML
    private ImageView lensImageView;
    @FXML
    private ImageView writePostImageview;
    @FXML
    private ListView yourAnswersListView;
    @FXML
    private ImageView logOutImageView;
    @FXML
    private Label locationLabel;
    @FXML
    private Label creationDateLabel;
    @FXML
    private Label reputationLabel;
    @FXML
    private Label webSiteLabel;


    public ControllerProfileInterface() {
        this.serverConnectionManager = ClientInterface.getServerConnectionManager();
        this.postObservableList = FXCollections.observableArrayList();
        this.userObservableList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize(){
        this.myPostsListView.setItems(this.postObservableList);
        this.myPostsListView.setCellFactory(plv->new ControllerPostBriefViewCell(PageType.PROFILE_INTERFACE));
        this.peopleYouMightKnowListView.setItems(this.userObservableList);
        this.peopleYouMightKnowListView.setCellFactory(plv->new ControllerFriendOfFriendsViewCell());
    }

    @FXML
    private void eventButtonBrowse(ActionEvent actionEvent) {
        ClientInterface.switchScene(PageType.POSTSEARCHINTERFACE);
    }

    @FXML
    private void eventButtonWrite(ActionEvent actionEvent) {
        ClientInterface.switchScene(PageType.WRITE);
    }

    @FXML
    private void eventButtonLogout(ActionEvent actionEvent) {
        try {
            serverConnectionManager.send(new MessageLogOut(ClientInterface.getLog().getDisplayName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fillProfileInterface(User u){
        //private ListView<Post> myPostsListView;
        //private ListView<User> peopleYouMightKnowListView;
        if (u.getProfileImage() != null) {
            profileImageImageView.setImage(new Image(u.getProfileImage()));
        }
        displayNameLabel.setText(u.getDisplayName());
        aboutMeWebView.getEngine().loadContent(u.getAboutMe());
        locationLabel.setText(u.getLocation());
        creationDateLabel.setText(User.convertMillisToDate(u.getCreationDate()).toString());
        reputationLabel.setText(Integer.toString(u.getReputation()));
        webSiteLabel.setText(u.getWebsiteURL());
    }

    @FXML
    public void logOut(Event event) {
    }

    @FXML
    public void switchToWritePostInterface(Event event) {
    }

    @FXML
    public void switchToPostSearchInterface(Event event) {
    }


    public void handleLogOutResponse() {
        Platform.runLater(() -> {
            ClientInterface.resetLog();
            ClientInterface.resetPostSearchInterface();
            ClientInterface.switchScene(PageType.POSTSEARCHINTERFACE);
        });
    }
}
