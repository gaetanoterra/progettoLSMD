package it.unipi.dii.client.controllers;


import it.unipi.dii.Libraries.Answer;
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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.ArrayList;


public class ControllerProfileInterface {

    private ServerConnectionManager serverConnectionManager;

    @FXML
    private ListView<Post> myPostsListView;
    private ObservableList<Post> postObservableList;
    @FXML
    private ListView<Answer> list_view_answers;
    private ObservableList<Answer> answersObservableList;
    @FXML
    private ListView<User> list_view_followers;
    private ObservableList<User> followersObservableList;
    @FXML
    private ListView<User> list_view_who_i_follow;
    private ObservableList<User> whoIFollowObservableList;
    @FXML
    private ListView<User> list_view_correlated_users;
    private ObservableList<User> userCorrelatedObservableList;
    @FXML
    private ListView<User> list_view_recommended_users;
    private ObservableList<User> userRecommendedObservableList;
    @FXML
    private Label displayNameLabel;
    @FXML
    private WebView aboutMeWebView;
    @FXML
    private ImageView profileImageImageView;
    @FXML
    private Button button_search_recommended_users, button_stats, button_delete_account;
    @FXML
    private TextField text_field_recommended_users;
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
        this.answersObservableList = FXCollections.observableArrayList();
        this.followersObservableList = FXCollections.observableArrayList();
        this.whoIFollowObservableList = FXCollections.observableArrayList();
        this.userCorrelatedObservableList = FXCollections.observableArrayList();
        this.userRecommendedObservableList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        this.myPostsListView.setItems(this.postObservableList);
        this.myPostsListView.setCellFactory(plv->new ControllerMyQuestionViewCell(PageType.PROFILE_INTERFACE));

        this.list_view_answers.setItems(this.answersObservableList);
        this.list_view_answers.setCellFactory((plv->new ControllerAnswerBriefViewCell(PageType.PROFILE_INTERFACE,
                                                                                        serverConnectionManager)));

        this.list_view_followers.setItems(this.followersObservableList);
        this.list_view_followers.setCellFactory((plv->new ControllerUserBriefViewCell(PageType.PROFILE_INTERFACE,
                                                                                        serverConnectionManager)));

        this.list_view_who_i_follow.setItems(this.whoIFollowObservableList);
        this.list_view_who_i_follow.setCellFactory((plv->new ControllerUserBriefViewCell(PageType.PROFILE_INTERFACE,
                                                                                        serverConnectionManager)));

        this.list_view_correlated_users.setItems(this.userCorrelatedObservableList);
        this.list_view_correlated_users.setCellFactory((plv->new ControllerUserBriefViewCell(PageType.PROFILE_INTERFACE,
                serverConnectionManager)));

        this.list_view_recommended_users.setItems(this.userRecommendedObservableList);
        this.list_view_recommended_users.setCellFactory((plv->new ControllerUserBriefViewCell(PageType.PROFILE_INTERFACE,
                serverConnectionManager)));

    }

    public void fillProfileInterface(User u) throws IOException {
        //carico subito i post
        serverConnectionManager.send(new MessageGetPostsByParameter(Parameter.Username, serverConnectionManager.getLoggedUser().getDisplayName()));

        //serverConnectionManager.send(new MessageGetAnswers(serverConnectionManager.getLoggedUser().getDisplayName()));
        //serverConnectionManager.send(new MessageGetFollowData(null, serverConnectionManager.getLoggedUser().getDisplayName(), true));
        //serverConnectionManager.send(new MessageGetFollowData(null, serverConnectionManager.getLoggedUser().getDisplayName(), false));
        //serverConnectionManager.send(new MessageGetCorrelatedUsers(null, serverConnectionManager.getLoggedUser().getDisplayName()));

        Platform.runLater(() -> {

            if (u.getProfileImage() != null) {
                profileImageImageView.setImage(new Image(u.getProfileImage()));
            }

            displayNameLabel.setText(u.getDisplayName());
            aboutMeWebView.getEngine().loadContent(u.getAboutMe());
            locationLabel.setText(u.getLocation());
            creationDateLabel.setText(User.convertMillisToDate(u.getCreationDate()).toString());
            reputationLabel.setText(Integer.toString(u.getReputation()));
            webSiteLabel.setText(u.getWebsiteURL());

            button_stats.setVisible(ClientInterface.getLog().isAdmin());
        });
    }

    //fill the list of user's posts
    public void fillPersonalUserPostInterface(ArrayList<Post> posts){
        Platform.runLater(() -> {
            postObservableList.clear();
            postObservableList.addAll(posts);
        });
    }

    //fill the list of user's answers
    public void fillAnswersUsers(ArrayList<Answer> answers) {
        Platform.runLater(() -> {
            if (answers != null) {
                answersObservableList.clear();
                answersObservableList.addAll(answers);
            }
        });
    }

    //fill the list of user's correlated users
    public void fillPersonalCorrelatedUsers(ArrayList<User> users){
        Platform.runLater(() -> {
            userCorrelatedObservableList.clear();
            userCorrelatedObservableList.addAll(users);
        });
    }

    //fill the list of user's recommended users
    public void fillPersonalRecommendedUsers(ArrayList<User> users){
        Platform.runLater(() -> {
            if (users != null) {
                userRecommendedObservableList.clear();
                userRecommendedObservableList.addAll(users);
            }
        });
    }

    //coloro che mi seguono
    public void fillFollowerList(ArrayList<User> followers) {
        Platform.runLater(() -> {
            if (followers != null) {
                followersObservableList.clear();
                followersObservableList.addAll(followers);
            }
        });
    }

    //coloro che io seguo
    public void fillFollowedList(ArrayList<User> followers) {
        Platform.runLater(() -> {
            if (followers != null) {
                whoIFollowObservableList.clear();
                whoIFollowObservableList.addAll(followers);
            }
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


    public void eventSearchRecommendedUsers(ActionEvent actionEvent) throws IOException {

        User u = serverConnectionManager.getLoggedUser();
        String displayName = u.getDisplayName();

        String tag = text_field_recommended_users.getText();

        MessageGetRecommendedUsers messageGetRecommendedUsers = new MessageGetRecommendedUsers(displayName, tag, null);

        serverConnectionManager.send(messageGetRecommendedUsers);
    }

    public void eventButtonStats(ActionEvent actionEvent) throws IOException {
        //ClientInterface.resetInterface();
        ClientInterface.switchScene(PageType.ANALYSIS_INTERFACE);
        ClientInterface.initAnalyticsInterface(PageType.PROFILE_INTERFACE);
    }


    public void eventButtonDeleteAccount(ActionEvent actionEvent) throws IOException {

        User u = serverConnectionManager.getLoggedUser();
        MessageUser messageUser = new MessageUser(Opcode.Message_User, OperationCD.Delete, u);

        serverConnectionManager.send(messageUser);
    }

    public void loadMyAnswers(Event event) throws IOException {
        serverConnectionManager.send(new MessageGetAnswers(serverConnectionManager.getLoggedUser().getDisplayName()));
    }

    public void loadMyFollowers(Event event) throws IOException {
        serverConnectionManager.send(new MessageGetFollowData(null, serverConnectionManager.getLoggedUser().getDisplayName(), true));
    }

    public void loadWhoIFollow(Event event) throws IOException {
        serverConnectionManager.send(new MessageGetFollowData(null, serverConnectionManager.getLoggedUser().getDisplayName(), false));
    }

    public void loadCorrelatedUsers(Event event) throws IOException {
        serverConnectionManager.send(new MessageGetCorrelatedUsers(null, serverConnectionManager.getLoggedUser().getDisplayName()));
    }

    public void resetInterface() {
        postObservableList.removeAll();
    }

    public void resetCorrelatedInterface() {
        userCorrelatedObservableList.removeAll();
    }

    public void resetRecommendedInterface() {
        userRecommendedObservableList.removeAll();
    }

    public void resetAnswersInterface() {
        answersObservableList.removeAll();
    }

    public void resetFollowerInterface() {
        followersObservableList.removeAll();
    }

    public void resetFollowedInterface() {
        whoIFollowObservableList.removeAll();
    }
}
