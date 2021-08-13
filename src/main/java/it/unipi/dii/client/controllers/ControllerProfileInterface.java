package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageGetPostByParameter;
import it.unipi.dii.Libraries.Messages.MessageUser;
import it.unipi.dii.Libraries.Messages.Opcode;
import it.unipi.dii.Libraries.Messages.Parameter;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import javax.swing.text.html.ImageView;
import java.io.IOException;

//classe preposta a gestire l'interfaccia del profilo utente
public class ControllerProfileInterface {

    private boolean modificable = false;
    private static ServerConnectionManager clientServerManager;
    private User[] suggestedUsers;
    private Post[] usersPosts;

    @FXML
    private TextArea textarea_aboutme;
    @FXML
    private TextField textfield_location, textfield_cretiondate, textfield_reputation, textfield_url;
    @FXML
    private ImageView imageview_profile;
    @FXML
    private Label label_displayname;
    @FXML
    private Button button_browse, button_write, button_logout, button_modify;
    @FXML
    private Pane pane_mypost, pane_suggested_users;
    @FXML
    private ChoiceBox choicebox_filters;

    public ControllerProfileInterface() {
        this.clientServerManager = ClientInterface.getServerConnectionManager();
    }

    public void eventButtonLogout(ActionEvent actionEvent) throws IOException {
        ClientInterface.switchScene(PageType.POSTSEARCHINTERFACE);
    }

    public String getAboutMe(){
        return textarea_aboutme.getText();
    }

    public String getDisplayName(){
        return label_displayname.getText();
    }

    public String getFilter(){
        return choicebox_filters.getId();
    }

    public void setDisplayName(String username){
        label_displayname.setText(username);
    }

    public void setAboutMe(String aboutme){
        textarea_aboutme.setText(aboutme);
    }

    //funzione che riempie i campi dell'interfaccia del profilo
    public void fillProfileInterface(User user) throws IOException {
        setDisplayName(user.getDisplayName());
        setAboutMe(user.getAboutMe());

        textfield_location.setText(user.getLocation());
        textfield_cretiondate.setText(user.getCreationData().toString());
        textfield_reputation.setText(String.valueOf(user.getReputation()));
        textfield_url.setText(user.getWebsiteURL());
        
        fillPosts();
        fillSuggestedUsers();
    }

    //funzione che riempie i campi del pannello dei post
    private void fillPosts() throws IOException {
        clientServerManager.send(new MessageGetPostByParameter(Parameter.Username, ClientInterface.getLog().getDisplayName()));
    }

    //richiedere gli utenti suggeriti
    private void fillSuggestedUsers() {

    }

    //funzione che mi porta all'interfaccia dove vedere i post
    public void eventButtonBrowse(ActionEvent actionEvent) throws IOException { ClientInterface.switchScene(PageType.POSTSEARCHINTERFACE); }

    //funzione che mi porta all'interfaccia dove scrivere un nuovo post
    public void eventButtonWrite(ActionEvent actionEvent) throws IOException { ClientInterface.switchScene(PageType.WRITE); }

    //funzione per rendere non editabili i textfield
    public void lockTextArea(){
        textfield_location.setEditable(false);
        textfield_reputation.setEditable(false);
        textfield_cretiondate.setEditable(false);
        textfield_url.setEditable(false);
        textarea_aboutme.setEditable(false);
    }

    //funzione per inviare le modifiche dell'utente
    public void eventButtonModify(ActionEvent actionEvent) throws IOException {
        if(!modificable) {
            modificable = true;

            textfield_location.setEditable(true);
            textfield_url.setEditable(true);
            textarea_aboutme.setEditable(true);

            button_modify.setText("Save");
        }
        else{
            modificable = false;
            User user = ClientInterface.getLog();
            user.setAboutMe(textarea_aboutme.getText());
            user.setLocation(textfield_location.getText());
            user.setWebsiteURL(textfield_url.getText());

            lockTextArea();

            button_modify.setText("Modify");

            clientServerManager.send(new MessageUser(Opcode.Message_Update_User_data, null, user));
        }
    }
}
