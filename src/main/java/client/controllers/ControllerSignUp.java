package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

import server.*;
import middleware.*;

//classe preposta a gestire l'interfaccia della registrazione utente
public class ControllerSignUp {

    ClientServerManager clientServerManager;

    @FXML
    private Label label_error_message_signup;
    @FXML
    private TextField textfield_signup_username;
    @FXML
    private PasswordField passwordfield_signup_password;

    public ControllerSignUp(){
        this.clientServerManager = ClientInterface.getClientServerManager();

    }
    public void eventButtonConfirmSignUp(ActionEvent actionEvent) throws IOException, InterruptedException {

        User user = new User();
        user.setDisplayName(textfield_signup_username.getText());
        user.setPassword(passwordfield_signup_password.getText());
        this.clientServerManager.send(new MessageSignUp(user));

        if(clientServerManager.checkLastServerAnswer()){
            label_error_message_signup.setText("");
            ControllerMessage.setTextArea("Registrazione avvenuta con successo!");
            ClientInterface.switchScene(PageType.MESSAGE);
        }
        else{
            label_error_message_signup.setText("Username già in uso");
        }
    }

    public void eventButtonCloseSignup(ActionEvent actionEvent) {
        ClientInterface.switchScene(PageType.ANONYMOUS_INTERFACE);
    }
}
