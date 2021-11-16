package it.unipi.dii.client.controllers;


import it.unipi.dii.Libraries.Messages.*;
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
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.ArrayList;

import static it.unipi.dii.client.ClientInterface.DEFAULT_USERNAME;


public class ControllerProfileInterface {

    private ServerConnectionManager serverConnectionManager;

    @FXML
    private ListView<Post> myPostsListView;
    private ObservableList<Post> postObservableList;
    @FXML
    private ListView<Post> myAnswersListView;
    private ObservableList<Post> answerObservableList;
    @FXML
    private Label displayNameLabel;
    @FXML
    private WebView aboutMeWebView;
    @FXML
    private ImageView profileImageImageView;
    @FXML
    private ListView<String> peopleYouMightKnowListView;
    private ObservableList<String> userObservableList;
    @FXML
    private ImageView lensImageView;
    @FXML
    private ImageView writePostImageview;
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
        this.answerObservableList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        this.myPostsListView.setItems(this.postObservableList);
        this.myPostsListView.setCellFactory(plv->new ControllerPostBriefViewCell(PageType.PROFILE_INTERFACE));
        this.peopleYouMightKnowListView.setItems(this.userObservableList);
        this.peopleYouMightKnowListView.setOrientation(Orientation.VERTICAL);
        //this.peopleYouMightKnowListView.setCellFactory(plv->new ControllerFriendOfFriendsViewCell());
        this.myAnswersListView.setItems(this.answerObservableList);
        this.myAnswersListView.setCellFactory(plv->new ControllerAnswerBriefViewCell(PageType.PROFILE_INTERFACE));
        this.myAnswersListView.setOrientation(Orientation.VERTICAL);
    }

    public void fillProfileInterface(User u) throws IOException {
        serverConnectionManager.send(new MessageGetPostByParameter(Parameter.Username, serverConnectionManager.getLoggedUser().getDisplayName()));
        serverConnectionManager.send(new MessageGetUserFollowers(null, serverConnectionManager.getLoggedUser().getDisplayName()));
        serverConnectionManager.send(new MessageGetAnswerData(null, serverConnectionManager.getLoggedUser().getDisplayName()));
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

    public void fillPersonalUserPostInterface(ArrayList<Post> posts){
        Platform.runLater(() -> {
            postObservableList.clear();
            postObservableList.addAll(posts);

            for (Post p:postObservableList) {
                System.out.println(p.getTags());
            }
        });
    }

    public void fillPersonalUserFollowers(ArrayList<String> users){
        Platform.runLater(() -> {
            userObservableList.clear();
            userObservableList.addAll(users);
        });
    }

    public void fillPersonaleUserAnswers(ArrayList<Post> answers){
        Platform.runLater(() -> {
            answerObservableList.clear();
            answerObservableList.addAll(answers);
        });
    }

    @FXML
    public void logOut(Event event) {
        try {
            serverConnectionManager.send(new MessageLogOut(ClientInterface.getLog().getDisplayName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void switchToWritePostInterface(Event event) {
        ClientInterface.switchScene(PageType.WRITE);
    }

    @FXML
    public void switchToPostSearchInterface(Event event) {
        ClientInterface.switchScene(PageType.POSTSEARCHINTERFACE);
    }


    public void handleLogOutResponse() {
        Platform.runLater(() -> {
            ClientInterface.resetLog();
            ClientInterface.resetPostSearchInterface();
            ClientInterface.switchScene(PageType.POSTSEARCHINTERFACE);
        });
    }
}
