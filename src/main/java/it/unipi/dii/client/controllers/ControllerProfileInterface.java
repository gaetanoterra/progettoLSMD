package it.unipi.dii.client.controllers;


import it.unipi.dii.Libraries.Messages.MessageLogOut;
import it.unipi.dii.Libraries.Messages.MessageUser;
import it.unipi.dii.Libraries.Messages.Opcode;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;


public class ControllerProfileInterface {

    private ServerConnectionManager serverConnectionManager;

    @FXML
    private ListView<Post> myPostsListView;
    private ObservableList<Post> postObservableList;
    @FXML
    private ListView<Post> list_view_answers;
    private ObservableList<Post> answersObservableList;
    @FXML
    private ListView<String> list_view_correlated_users;
    private ObservableList<String> userCorrelatedObservableList;
    @FXML
    private ListView<String> list_view_recommended_users;
    private ObservableList<String> userRecommendedObservableList;
    @FXML
    private Label displayNameLabel;
    @FXML
    private WebView aboutMeWebView;
    @FXML
    private ImageView profileImageImageView;
    @FXML
    private Button button_search_recommended_users, button_stats;
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
        this.userCorrelatedObservableList = FXCollections.observableArrayList();
        this.userRecommendedObservableList = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        this.myPostsListView.setItems(this.postObservableList);
        this.myPostsListView.setCellFactory(plv->new ControllerMyQuestionViewCell(PageType.PROFILE_INTERFACE));

        this.list_view_answers.setItems(this.answersObservableList);
        this.list_view_answers.setCellFactory((plv->new ControllerAnswerBriefViewCell(PageType.PROFILE_INTERFACE, serverConnectionManager)));

        this.list_view_correlated_users.setItems(this.userCorrelatedObservableList);

        this.list_view_recommended_users.setItems(this.userRecommendedObservableList);

        /*if(!serverConnectionManager.getLoggedUser().isAdmin())
            button_stats.setDisable(true);*/
    }

    public void fillProfileInterface(User u) throws IOException {
        serverConnectionManager.send(new MessageGetPostByParameter(Parameter.Username, serverConnectionManager.getLoggedUser().getDisplayName()));
        serverConnectionManager.send(new MessageGetAnswers(serverConnectionManager.getLoggedUser().getDisplayName()));
        serverConnectionManager.send(new MessageGetCorrelatedUsers(null, serverConnectionManager.getLoggedUser().getDisplayName()));

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
        });
    }

    public void fillPersonalUserPostInterface(ArrayList<Post> posts){
        Platform.runLater(() -> {
            postObservableList.clear();
            postObservableList.addAll(posts);
        });
    }

    public void fillAnswersUsers(ArrayList<Post> answers) {
        Platform.runLater(() -> {
            if (answers != null) {
                answersObservableList.clear();
                answersObservableList.addAll(answers);
            }
        });
    }

    public void fillPersonalCorrelatedUsers(ArrayList<String> users){
        Platform.runLater(() -> {
            userCorrelatedObservableList.clear();
            userCorrelatedObservableList.addAll(users);
        });
    }

    public void fillPersonalRecommendedUsers(ArrayList<String> users){
        Platform.runLater(() -> {
            if (users != null) {
                userRecommendedObservableList.clear();
                userRecommendedObservableList.addAll(users);
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


    public void eventSearchRecommendedUsers(ActionEvent actionEvent) throws IOException {
        serverConnectionManager.send(new MessageGetRecommendedUsers(serverConnectionManager.getLoggedUser().getDisplayName(), text_field_recommended_users.getText(), null));
    }

    public void eventSelectItemCorrelated(MouseEvent mouseEvent) throws IOException {
       /* serverConnectionManager.send(
                new MessageGetUserData(
                        new ArrayList<>(
                                List.of(
                                        new User(
                                                null,
                                                    list_view_correlated_users.getSelectionModel().getSelectedItem(),
                                                null,
                                                null,
                                                null,
                                                null
                                        )
                                )
                        ),
                        true, PageType.PROFILE_INTERFACE)
        );*/
    }

    public void eventSelectItemRecommended(MouseEvent mouseEvent) throws IOException {
      //  serverConnectionManager.send(new MessageGetUserData(new ArrayList<>(List.of(new User(null, list_view_recommended_users.getSelectionModel().getSelectedItem(), null, null, null, null))), true, PageType.PROFILE_INTERFACE));
    }

    public void eventButtonStats(ActionEvent actionEvent) throws IOException {
        //ClientInterface.resetInterface();
        ClientInterface.switchScene(PageType.ANALYSIS_INTERFACE);
        ClientInterface.initAnalyticsInterface(PageType.PROFILE_INTERFACE);
    }

    public void eventButtonDeleteAccount(ActionEvent actionEvent) throws IOException {
        serverConnectionManager.send(new MessageUser(Opcode.Message_User, OperationCD.Delete, serverConnectionManager.getLoggedUser()));
    }
}
