package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageGetPostByParameter;
import it.unipi.dii.Libraries.Messages.Parameter;
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

import java.io.IOException;
import java.util.List;

//classe preposta a gestire l'interfaccia del profilo utente
public class ControllerExternalUserInterface {

    private ServerConnectionManager serverConnectionManager;
    private User user;
    private PageType lastPageVisited;
    private ObservableList<Post> postObservableList;

    @FXML
    private Label label_display_name = new Label(), label_location = new Label(), label_creation_date = new Label(), label_reputation = new Label(), label_website = new Label();
    @FXML
    private ListView<Post> myPostsListView;
    @FXML
    private TextArea text_area_aboutme;
    @FXML
    private ImageView profileImageImageView;
    @FXML
    private Button button_deleteUser, button_lback;


    public ControllerExternalUserInterface() {
        this.postObservableList = FXCollections.observableArrayList();
        this.serverConnectionManager = ClientInterface.getServerConnectionManager();
    }

    public void initialize(User user, PageType lastPageVisited) throws IOException {
        this.user = user;
        this.lastPageVisited = lastPageVisited;
        this.text_area_aboutme.setEditable(false);
        //user is logged, is admin, and it's not its own profile (only admin can remove others)
        this.button_deleteUser.setVisible(ClientInterface.getLog() != null && ClientInterface.getLog().isAdmin() && !ClientInterface.getLog().getUserId().equals(user.getUserId()));

        myPostsListView.setItems(postObservableList);
        this.myPostsListView.setCellFactory(plv->new ControllerPostBriefViewCell(PageType.EXTERNAL_PROFILE));
        fillExternalUserInterface();

        //sending the request for user posts
        this.serverConnectionManager.send(new MessageGetPostByParameter(Parameter.Username, user.getDisplayName()));
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
    }

    public void fillExternalUserPosts(List<Post> posts){
        postObservableList.setAll(posts);
    }

    @FXML
    private void eventButtonBack(ActionEvent actionEvent) {
        ClientInterface.switchScene(lastPageVisited);
    }

    @FXML
    private void eventButtonDeleteUser(ActionEvent actionEvent) {
        ClientInterface.deleteUser(user);
        // back to zero
        ClientInterface.switchScene(PageType.POST_SEARCH_INTERFACE);
    }
}
