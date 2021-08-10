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

import java.io.IOException;
import java.util.ArrayList;

//classe preposta a gestire l'interfaccia da cui si visualizzano i Post
public class ControllerPostSearchInterface {

    private ServerConnectionManager serverConnectionManager;

    private static Post last_post_seen;

    private ObservableList<Post> postObservableList;

    @FXML
    private Label label_username_anonymous_interface;
    @FXML
    private Button signin_button, signup_button, search_button, profile_button;

    @FXML
    private TextField textfield_search;
    @FXML
    private ListView<Post> postsListView;

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

    //imposto cosa posso utilizzare e cosa no a seconda che l'utente sia loggato o meno
    public void setLoggedInterface(String username){
        label_username_anonymous_interface.setText(username);
        signin_button.setDisable(true);
        signup_button.setDisable(true);
        profile_button.setDisable(false);
    }

    //rendo di nuovo disponibili i bottoni e scollego l'interfaccia dall'utente
    public void resetInterface(){
        this.postObservableList.removeAll();
    }

    //metodo per inserire i post nel panello
    public void fillPostPane(ArrayList<Post> postArrayList) {
        this.postObservableList.setAll(postArrayList);
    }


    public void eventButtonSignIn(ActionEvent event) throws IOException {
        ClientInterface.switchScene(PageType.SIGN_IN);
    }

    public void eventButtonSignUp(ActionEvent event) throws IOException {
        ClientInterface.switchScene(PageType.SIGN_UP);
    }

    public void eventButtonProfile(ActionEvent actionEvent) {
        //
        // ClientInterface.getControllerProfileInterface().lockTextArea();
    }


    //event che invia la richiesta al ClientManager in base al parametro di ricerca
    public void eventButtonSearch(ActionEvent actionEvent) throws IOException {
       resetInterface();
       serverConnectionManager.send(new MessageGetPostByParameter(Parameter.Text,textfield_search.getText()));
    }

    public void getFullPostInterface(String id){


    }


    public static Post lastPostSeen(){
        return last_post_seen;
    }

    public void fillInterface(Post post) {
    }
}
