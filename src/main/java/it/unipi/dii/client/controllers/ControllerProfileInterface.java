package it.unipi.dii.client.controllers;


import it.unipi.dii.Libraries.Messages.MessageLogOut;
import it.unipi.dii.Libraries.Messages.MessageUser;
import it.unipi.dii.Libraries.Messages.Opcode;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;

import java.io.IOException;


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
    private TextField locationLabel;
    @FXML
    private TextField creationDateLabel;
    @FXML
    private TextField reputationLabel;
    @FXML
    private TextField webSiteLabel;
    @FXML
    private Button button_modify;
    /*
    @FXML
    private TextArea textarea_aboutme;
    @FXML
    private TextField textfield_location, textfield_creationdate, textfield_reputation, textfield_url;
     */
    private boolean modificable = false;


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

    public void fillProfileInterface(User u){
        //private ListView<Post> myPostsListView;
        //private ListView<User> peopleYouMightKnowListView;
        if (u.getProfileImage() != null) {
            profileImageImageView.setImage(new Image(u.getProfileImage()));
        }
        displayNameLabel.setText(u.getDisplayName());
        aboutMeWebView.getEngine().loadContent(u.getAboutMe());
        aboutMeWebView.setContextMenuEnabled(false);
        locationLabel.setText(u.getLocation());
        creationDateLabel.setText(User.convertMillisToDate(u.getCreationDate()).toString());
        reputationLabel.setText(Integer.toString(u.getReputation()));
        webSiteLabel.setText(u.getWebsiteURL());
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
        ClientInterface.switchScene(PageType.POST_SEARCH_INTERFACE);
    }


    public void handleLogOutResponse() {
        Platform.runLater(() -> {
            ClientInterface.resetLog();
            ClientInterface.resetPostSearchInterface();
            ClientInterface.switchScene(PageType.POST_SEARCH_INTERFACE);
        });
    }

    public synchronized void lockModifyText(boolean block){
        this.modificable = !block;
        locationLabel.setEditable(this.modificable);
        webSiteLabel.setEditable(this.modificable);
        if (this.modificable) {
            aboutMeWebView.getEngine().loadContent("<body contenteditable=\"true\" style=\"display: inline-block;\">" + ClientInterface.getLog().getAboutMe() + "</body>");
        }
        else {
            String aboutMe = (String) aboutMeWebView.getEngine().executeScript("decodeURI(document.body.innerHTML)");
            System.out.println(aboutMe);
            aboutMeWebView.getEngine().loadContent(aboutMe);
        }

    }

    @FXML
    public void eventButtonModify(MouseEvent mouseEvent) throws IOException {
        if (!modificable) {
            lockModifyText(false);
            button_modify.setText("Save");
        } else {
            lockModifyText(true);
            User user = ClientInterface.getLog();
            String aboutMe = (String) aboutMeWebView.getEngine().executeScript("decodeURI(document.body.innerHTML)");
            aboutMeWebView.getEngine().loadContent(aboutMe);
            user.setAboutMe(aboutMe);
            user.setLocation(locationLabel.getText());
            user.setWebsiteURL(webSiteLabel.getText());

            button_modify.setText("Modify");
            serverConnectionManager.send(
                    new MessageUser(
                            Opcode.Message_Update_User_data, user
                    )
            );
        }
    }

}
