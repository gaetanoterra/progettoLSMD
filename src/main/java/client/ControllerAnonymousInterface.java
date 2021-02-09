package client;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import server.*;
import middleware.*;

import java.io.IOException;

//classe preposta a gestire l'interfaccia da cui si visualizzano i Post
public class ControllerAnonymousInterface {

    private ClientServerManager clm = Main.getClientServerManager();

    private static Parameter searchMethod = Parameter.Date;
    private static String filter = "interesting";
    private static Post[] posts;

    @FXML
    private static Label label_username_anonymous_interface;
    @FXML
    private static Button signin_button, signup_button, profile_button, sort_interesting, sort_hot, sort_week, sort_month;
    @FXML
    private static RadioButton radio_button_date, radio_button_tags, radio_button_username, radio_button_text;
    @FXML
    private static TextField textfield_search;

    //imposto cosa posso utilizzare e cosa no a seconda che l'utente sia loggato o meno
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

    //metodo per inserire i post nel panello
    public static void fillPostPane() {
        //ordino i post in base al sort
        ordinaPost();
    }

    //ordino i post in base al sort
    private static void ordinaPost() {
        if(filter.equals("interesting")){

        }
        else if(filter.equals("hot")){

        }
        else if(filter.equals("week")){

        }
        else if(filter.equals("month")){

        }
    }

    //funzione chiamata da ClientServerManager per fornire i post da visualizzare
    public static void setPosts(Post[] post) {
        posts = post;
        fillPostPane();
    }

    public void eventButtonSignIn(ActionEvent event){
        Main.switchScene(1);
    }

    public void eventButtonSignUp(ActionEvent event){
        Main.switchScene(2);
    }

    public void eventButtonProfile(ActionEvent actionEvent) {
        Main.switchScene(3);
        ControllerProfileInterface.lockTextArea();
    }

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

    //event che invia la richiesta al ClientManager in base al parametro di ricerca
    public void eventButtonSearch(ActionEvent actionEvent) throws IOException {
        if(searchMethod == Parameter.Date)
            clm.send(new MessageGetPostByParameter(Parameter.Date, textfield_search.getText(), null));

        else if(searchMethod == Parameter.Tags)
            clm.send(new MessageGetPostByParameter(Parameter.Tags, textfield_search.getText(), null));

        else if(searchMethod == Parameter.Text)
            clm.send(new MessageGetPostByParameter(Parameter.Text, textfield_search.getText(), null));

        else if(searchMethod == Parameter.Username)
            clm.send(new MessageGetPostByParameter(Parameter.Username, textfield_search.getText(), null));
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
}
