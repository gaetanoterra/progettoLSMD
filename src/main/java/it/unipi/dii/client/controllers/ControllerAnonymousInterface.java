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
import java.util.Date;
import java.util.List;

//classe preposta a gestire l'interfaccia da cui si visualizzano i Post
public class ControllerAnonymousInterface {

    private ServerConnectionManager clientServerManager;

    private static Parameter searchMethod;
    private static String filter = "interesting";
    private static Post[] posts;
    private static Post last_post_seen;

    private ObservableList<Post> postObservableList;

    @FXML
    private Label label_username_anonymous_interface;
    @FXML
    private Button signin_button, signup_button, search_button, profile_button, sort_interesting, sort_hot, sort_week, sort_month;
    @FXML
    private RadioButton radio_button_date, radio_button_tags, radio_button_username, radio_button_text;
    @FXML
    private TextField textfield_search;
    @FXML
    private ListView<Post> postsListView;

    public ControllerAnonymousInterface(){
        this.postObservableList = FXCollections.observableArrayList();
        this.clientServerManager = ClientInterface.getServerConnectionManager();
    }

    @FXML
    private void initialize(){
        radio_button_text.setSelected(true);
        searchMethod = Parameter.Text;
        textfield_search.requestFocus();

        postsListView.setItems(this.postObservableList);
        postsListView.setCellFactory(plv->new PostBriefViewCell());

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
        this.postObservableList.addAll(postArrayList);
    }


    //ordino i post in base al sort
    private void ordinaPost() {
        switch (filter) {
            case "interesting":

                break;
            case "hot":

                break;
            case "week":

                break;
            case "month":

                break;
        }
    }


    public void eventButtonSignIn(ActionEvent event) throws IOException {
        ClientInterface.switchScene(PageType.SIGN_IN);
    }

    public void eventButtonSignUp(ActionEvent event) throws IOException {
        ClientInterface.switchScene(PageType.SIGN_UP);
    }

    public void eventButtonProfile(ActionEvent actionEvent) {
        // ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
        // ClientInterface.getControllerProfileInterface().lockTextArea();
    }

    public void eventSetSearchMethod(ActionEvent actionEvent) {
        if(radio_button_text.isSelected()) {
            searchMethod = Parameter.Text;
            return;
        }
        if(radio_button_date.isSelected()) {
            searchMethod = Parameter.Date;
            return;
        }
        if(radio_button_tags.isSelected()) {
            searchMethod = Parameter.Tags;
            return;
        }
        if(radio_button_username.isSelected()) {
            searchMethod = Parameter.Username;
        }
    }

    //event che invia la richiesta al ClientManager in base al parametro di ricerca
    public void eventButtonSearch(ActionEvent actionEvent) throws IOException {
        switch (searchMethod) {
            case Date -> clientServerManager.send(new MessageGetPostByParameter(Parameter.Date,
                                                                                            textfield_search.getText()));
            case Tags -> clientServerManager.send(new MessageGetPostByParameter(Parameter.Tags,
                                                                                            textfield_search.getText()));
            case Text -> clientServerManager.send(new MessageGetPostByParameter(Parameter.Text,
                                                                                            textfield_search.getText()));
            case Username -> clientServerManager.send(new MessageGetPostByParameter(Parameter.Username,
                                                                                            textfield_search.getText()));
        }

    }

    //event per impostare il valore del filtro
    public void eventSetFilter(ActionEvent actionEvent) {
        if(sort_interesting.isPressed())
            filter = "interesting";
        else if(sort_hot.isPressed())
            filter = "hot";
        else if(sort_week.isPressed())
            filter = "week";
        else if(sort_month.isPressed())
            filter = "month";
    }

    public static Post lastPostSeen(){
        return last_post_seen;
    }
}
