package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageGetPostByParameter;
import it.unipi.dii.Libraries.Messages.Parameter;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.ArrayList;

public class ControllerPostSearchInterface {

    private ServerConnectionManager serverConnectionManager;
    private ObservableList<Post> postObservableList;

    @FXML
    private Button signin_button, signup_button, search_button, button_analytics;
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

        postsListView.setItems(this.postObservableList);
        postsListView.setCellFactory(plv->new ControllerPostBriefViewCell());

    }

    public void setLoggedInterface(String username, String imageUrl){
        if(imageUrl != null)
            this.profileImageView.setImage(new Image(imageUrl));
        usernameLabel.setText(username);
        signin_button.setDisable(true);
        signup_button.setDisable(true);

    }

    //rendo di nuovo disponibili i bottoni e scollego l'interfaccia dall'utente
    public void resetInterface(){
        this.postObservableList.removeAll();
    }

    //metodo per inserire i post nel panello
    public void fillPostPane(ArrayList<Post> postArrayList) {
        this.postObservableList.setAll(postArrayList);
    }

    @FXML
    public void eventButtonSignIn(ActionEvent event) throws IOException {
        ClientInterface.switchScene(PageType.SIGN_IN);
    }

    @FXML
    public void eventButtonSignUp(ActionEvent event) throws IOException {
        ClientInterface.switchScene(PageType.SIGN_UP);
    }

    //event che invia la richiesta al ClientManager in base al parametro di ricerca
    @FXML
    public void eventButtonSearch(ActionEvent actionEvent) throws IOException {
       resetInterface();
       serverConnectionManager.send(new MessageGetPostByParameter(Parameter.Text,textfield_search.getText()));
    }

    public void eventAnalytics(ActionEvent actionEvent) throws IOException {
        resetInterface();
        ClientInterface.switchScene(PageType.ANALYSIS_INTERFACE);
        ClientInterface.initAnalyticsInterface(PageType.POSTSEARCHINTERFACE);
    }
}
