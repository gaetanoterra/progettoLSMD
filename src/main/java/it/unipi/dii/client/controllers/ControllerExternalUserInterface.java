package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageGetPostByParameter;
import it.unipi.dii.Libraries.Messages.Opcode;
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
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.ArrayList;

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


    public ControllerExternalUserInterface() {
        this.postObservableList = FXCollections.observableArrayList();
        this.serverConnectionManager = ClientInterface.getServerConnectionManager();
    }

    public void initialize(User user, PageType lastPageVisited) throws IOException {
        this.user = user;
        this.lastPageVisited = lastPageVisited;
        this.text_area_aboutme.setEditable(false);

        myPostsListView.setItems(postObservableList);
        this.myPostsListView.setCellFactory(plv->new ControllerPostBriefViewCell());
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

    public void fillExternalUserPosts(ArrayList<Post> posts){
        postObservableList.setAll(posts);
    }

    @FXML
    private void eventButtonBack(ActionEvent actionEvent) {
        ClientInterface.switchScene(lastPageVisited);
    }
}
