package client;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import server.*;
import middleware.*;

import java.io.IOException;

public class ControllerAnonymousInterface {

    private ClientServerManager clm = Main.getClientServerManager();

    private static Parameter searchMethod = Parameter.Date;
    private static String filter = "interesting";

    @FXML
    private static Label label_username_anonymous_interface;
    @FXML
    private static Button signin_button, signup_button, profile_button;
    @FXML
    private static RadioButton radio_button_date, radio_button_tags, radio_button_username, radio_button_text;
    @FXML
    private static TextField textfield_search;

    public static void setLoggedInterface(String username){
        label_username_anonymous_interface.setText(username);
        signin_button.setDisable(true);
        signup_button.setDisable(true);
        profile_button.setDisable(false);
    }

    //rendo di nuovo disponibili i bottoni e scollego l'interfaccia dall'utente
    public static void resetInterface(){
        label_username_anonymous_interface.setText("Anonymous");
        signin_button.setDisable(false);
        signup_button.setDisable(false);
        profile_button.setDisable(true);
    }

    public void eventButtonSignIn(ActionEvent event){
        Main.switchScene(1);
    }

    public void eventButtonSignUp(ActionEvent event){
        Main.switchScene(2);
    }

    public void eventButtonProfile(ActionEvent actionEvent) {Main.switchScene(3); }

    public void eventSetSearchMethod(ActionEvent actionEvent) {
        if(radio_button_date.isSelected())
            searchMethod = Parameter.Date;
        if(radio_button_tags.isSelected())
            searchMethod = Parameter.Tags;
        if(radio_button_text.isSelected())
            searchMethod = Parameter.Text;
        if(radio_button_username.isSelected())
            searchMethod = Parameter.Username;

    }

    public void eventButtonSearch(ActionEvent actionEvent) throws IOException {
        if(searchMethod.equals("date"))
            clm.send(new MessageGetPostByParameter(Parameter.Date, textfield_search.getText(), null));

        else if(searchMethod.equals("tags"))
            clm.send(new MessageGetPostByParameter(Parameter.Tags, textfield_search.getText(), null));

        else if(searchMethod.equals("text"))
            clm.send(new MessageGetPostByParameter(Parameter.Text, textfield_search.getText(), null));

        else if(searchMethod.equals("username"))
            clm.send(new MessageGetPostByParameter(Parameter.Username, textfield_search.getText(), null));
    }


    public void eventSetFilter(ActionEvent actionEvent) {
    }
}
