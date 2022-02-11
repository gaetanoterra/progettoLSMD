package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageGetPostsByParameter;
import it.unipi.dii.Libraries.Messages.Parameter;
import it.unipi.dii.Libraries.Post;
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
import java.net.URL;
import java.util.List;

import static it.unipi.dii.client.ClientInterface.DEFAULT_USERNAME;

public class ControllerPostSearchInterface {

    private ServerConnectionManager serverConnectionManager;
    private ObservableList<Post> postObservableList;

    @FXML
    private Button signin_button, signup_button, search_button, profile_button, button_analytics;
    @FXML
    private TextField textfield_search;
    @FXML
    private ListView<Post> postsListView;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Label usernameLabel;

    public ControllerPostSearchInterface(){
        this.postObservableList = FXCollections.observableArrayList();
        this.serverConnectionManager = ClientInterface.getServerConnectionManager();

    }

    @FXML
    private void initialize(){
        textfield_search.requestFocus();
        setLoggedOutInterface();
        postsListView.setItems(this.postObservableList);
        postsListView.setCellFactory(plv->new ControllerPostBriefViewCell(PageType.POST_SEARCH_INTERFACE));
    }

    public void setLoggedInterface(String username, String imageUrl){
        if(imageUrl != null)
            profileImageView.setImage(new Image(imageUrl));
        usernameLabel.setText(username);
        signin_button.setDisable(true);
        signup_button.setDisable(true);
        profile_button.setDisable(false);
        resetInterface();
    }

    public void setLoggedOutInterface(){
        URL urlImageAnonymous = getClass().getResource("/images/anonymous_user.png");
        if (urlImageAnonymous != null) {
            profileImageView.setImage(new Image(urlImageAnonymous.toString()));
        }
        usernameLabel.setText(DEFAULT_USERNAME);
        signin_button.setDisable(false);
        signup_button.setDisable(false);
        profile_button.setDisable(true);
        resetInterface();
    }

    //rendo di nuovo disponibili i bottoni e scollego l'interfaccia dall'utente
    public void resetInterface(){
        this.postObservableList.clear();
    }

    //metodo per inserire i post nel panello
    public void fillPostPane(List<Post> postArrayList) {
        Platform.runLater(()-> this.postObservableList.setAll(postArrayList));
     }

    @FXML
    public void eventButtonSignIn(ActionEvent event) {
        ClientInterface.switchScene(PageType.SIGN_IN);
    }

    @FXML
    public void eventButtonSignUp(ActionEvent event) {
        ClientInterface.switchScene(PageType.SIGN_UP);
    }

    //event che invia la richiesta al ClientManager in base al parametro di ricerca
    @FXML
    public void eventButtonSearch(ActionEvent actionEvent) throws IOException {
       resetInterface();
       serverConnectionManager.send(new MessageGetPostsByParameter(Parameter.Text,textfield_search.getText()));
    }

    @FXML
    public void eventButtonProfile(ActionEvent event) {
        ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
    }

}
