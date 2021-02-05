package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import server.*;
import middleware.*;

import javax.swing.text.html.ImageView;

public class ControllerProfileInterface {

    @FXML
    private static TextArea textarea_aboutme, textarea_info;
    @FXML
    private static ImageView imageview_profile;
    @FXML
    private static Label label_displayname;
    @FXML
    private static Button button_browse, button_write, button_logout;
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

    public String getInfo(){
        return textarea_info.getText();
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

    public static void setInfo(String info){
        textarea_info.setText(info);
    }

    public static void setAboutMe(String aboutme){
        textarea_aboutme.setText(aboutme);
    }

    public static void fillProfileInterface(User user){
        setDisplayName(user.getDisplayName());
        setAboutMe(user.getAboutMe());

        //probabilmente dovrà essere sostituito il text field delle info con delle text area singole
        String creationDate = user.getCreationData().toString();
        String website = user.getWebsiteURL();
        String location = user.getLocation();
        String reputation = String.valueOf(user.getReputation());

        String info = "Location:" + location + "\nReputation:" + reputation + "\nCreation date:" + creationDate + "\nWebsite:" + website;

        setInfo(info);
    }
}
