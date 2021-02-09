package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import server.*;
import middleware.*;

import javax.swing.text.html.ImageView;
import java.io.IOException;

public class ControllerProfileInterface {

    private boolean modificable = false;
    private static ClientServerManager clm = Main.getClientServerManager();
    private User[] suggestedUsers;
    private Post[] usersPosts;

    @FXML
    private static TextArea textarea_aboutme;
    @FXML
    private static TextField textfield_location, textfield_cretiondate, textfield_reputation, textfield_url;
    @FXML
    private static ImageView imageview_profile;
    @FXML
    private static Label label_displayname;
    @FXML
    private static Button button_browse, button_write, button_logout, button_modify;
    @FXML
    private static Pane pane_mypost, pane_suggested_users;
    @FXML
    private static ChoiceBox choicebox_filters;

    public void eventButtonLogout(ActionEvent actionEvent) {
        Main.switchScene(0);
    }

    public String getAboutMe(){
        return textarea_aboutme.getText();
    }

    public static String getDisplayName(){
        return label_displayname.getText();
    }

    public String getFilter(){
        return choicebox_filters.getId();
    }

    public static void setDisplayName(String username){
        label_displayname.setText(username);
    }

    public static void setAboutMe(String aboutme){
        textarea_aboutme.setText(aboutme);
    }

    public static void fillProfileInterface(User user) throws IOException {
        setDisplayName(user.getDisplayName());
        setAboutMe(user.getAboutMe());

        textfield_location.setText(user.getLocation());
        textfield_cretiondate.setText(user.getCreationData().toString());
        textfield_reputation.setText(String.valueOf(user.getReputation()));
        textfield_url.setText(user.getWebsiteURL());
        
        fillPosts();
        fillSuggestedUsers();
    }

    private static void fillPosts() throws IOException {
        clm.send(new MessageGetPostByParameter(Parameter.Username, Main.getLog().getDisplayName(), null));
    }

    //richiedere gli utenti suggeriti
    private static void fillSuggestedUsers() {
    }

    public void eventButtonBrowse(ActionEvent actionEvent){ Main.switchScene(0); }

    public void eventButtonWrite(ActionEvent actionEvent) { Main.switchScene(4); }

    public static void lockTextArea(){
        textfield_location.setEditable(false);
        textfield_reputation.setEditable(false);
        textfield_cretiondate.setEditable(false);
        textfield_url.setEditable(false);
        textarea_aboutme.setEditable(false);
    }

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
            User user = Main.getLog();
            user.setAboutMe(textarea_aboutme.getText());
            user.setLocation(textfield_location.getText());
            user.setWebsiteURL(textfield_url.getText());

            lockTextArea();

            button_modify.setText("Modify");

            clm.send(new MessageUser(Opcode.Message_Update_User_data, null, user));
        }
    }
}
