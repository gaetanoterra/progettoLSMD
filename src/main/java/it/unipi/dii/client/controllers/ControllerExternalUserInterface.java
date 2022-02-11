package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageGetPostsByParameter;
import it.unipi.dii.Libraries.Messages.Parameter;
import it.unipi.dii.Libraries.Messages.*;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.List;

//classe preposta a gestire l'interfaccia del profilo utente
public class ControllerExternalUserInterface {

    private ServerConnectionManager serverConnectionManager;
    private User user;
    private PageType lastPageVisited;
    private ObservableList<Post> postObservableList;
    private boolean followed;

    @FXML
    private Label label_display_name = new Label(), label_location = new Label(), label_creation_date = new Label(), label_reputation = new Label(), label_website = new Label();
    @FXML
    private ListView<Post> myPostsListView;
    @FXML
    private TextArea text_area_aboutme;
    @FXML
    private ImageView profileImageImageView;
    @FXML
    private Button button_follow, button_delete_account, button_lback;


    public ControllerExternalUserInterface() {
        this.postObservableList = FXCollections.observableArrayList();
        this.serverConnectionManager = ClientInterface.getServerConnectionManager();
    }

    public void initialize(User user, PageType lastPageVisited) throws IOException {
        this.user = user;
        this.lastPageVisited = lastPageVisited;
        this.text_area_aboutme.setEditable(false);

        myPostsListView.setItems(postObservableList);
        this.myPostsListView.setCellFactory(plv->new ControllerPostBriefViewCell(PageType.EXTERNAL_PROFILE));
        fillExternalUserInterface();

        //sending the request for user posts
        this.serverConnectionManager.send(new MessageGetPostsByParameter(Parameter.Username, user.getDisplayName()));
        this.serverConnectionManager.send(new MessageFollow(Opcode.Message_Follow, OperationCD.Check, user));
    }

    private void fillExternalUserInterface() {
        if (user.getDisplayName() != null)
            label_display_name.setText(user.getDisplayName());
        if (user.getLocation() != null)
            label_location.setText(user.getLocation());
        if (user.getCreationDate() != null)
            label_creation_date.setText(user.convertMillisToDate(user.getCreationDate()).toString());
        label_reputation.setText(Integer.toString(user.getReputation()));
        if (user.getWebsiteURL() != null)
            label_website.setText(user.getWebsiteURL());
        if (user.getAboutMe() != null)
            text_area_aboutme.setText(user.getAboutMe());

        try {
            profileImageImageView.setImage(new Image(user.getProfileImage()));
        }catch (IllegalArgumentException | NullPointerException e){
            profileImageImageView.setImage(new Image("/images/anonymous_user.png"));
        }

        //user is logged, is admin, and it's not its own profile (only admin can remove others)
        this.button_delete_account.setVisible(ClientInterface.getLog() != null && ClientInterface.getLog().isAdmin() && !ClientInterface.getLog().getUserId().equals(user.getUserId()));

    }

    public void fillExternalUserPosts(List<Post> posts){
        postObservableList.setAll(posts);
    }

    @FXML
    private void eventButtonBack(ActionEvent actionEvent) {
        ClientInterface.switchScene(lastPageVisited);
    }

    public void eventFollow(ActionEvent actionEvent) throws IOException {
        if (followed)
            serverConnectionManager.send(new MessageFollow(Opcode.Message_Follow, OperationCD.Delete, user));
        else
            serverConnectionManager.send(new MessageFollow(Opcode.Message_Follow, OperationCD.Create, user));
    }

    @FXML
    public void eventButtonDeleteAccount(ActionEvent actionEvent) throws IOException {
        ClientInterface.deleteUser(user);
    }

    public void setUnfollowUser() {
        Platform.runLater(()-> {
            button_follow.setText("Unfollow");
            followed = true;
        });

    }

    public void setFollowUser() {
        Platform.runLater(()-> {
            button_follow.setText("Follow");
            followed = false;
        });
    }

    public void setFollowUnfollowUser(User user) {
        if (user != null)
            setUnfollowUser();
        else
            setFollowUser();
    }

}
