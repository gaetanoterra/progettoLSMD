package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageLogin;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;

import java.io.IOException;

//classe preposta a gestire l'interfaccia del login
public class ControllerSignInInterface {

    ServerConnectionManager clm = ClientInterface.getServerConnectionManager();

    @FXML
    private Label label_error_message_signin;
    @FXML
    private TextField textfield_signin_username;
    @FXML
    private PasswordField passwordfield_signin_password;

    //invio opcode, username e password a client.ClientServerManager, il quale effettuerà la richiesta a server.ClientManager
    public void eventButtonConfirmSignIn(ActionEvent actionEvent) throws IOException, InterruptedException {

        User user = new User();
        user.setDisplayName(textfield_signin_username.getText());
        user.setPassword(passwordfield_signin_password.getText());

        clm.send(new MessageLogin(user));
        if(clm.checkLastServerAnswer()){
            label_error_message_signin.setText("");

            //mi serve una funzione che popola l'interfaccia del profilo utente con i dati dell'utente
            // (utente qualsiasi, in questo caso sarà dell'utente loggato ma se clicco sul profilo di un altro utente lo visualizzo nellos tesso modo)

            //switch della pagina
            ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
            // ClientInterface.getControllerProfileInterface().lockTextArea();
        }
        else{
            label_error_message_signin.setText("*Username o password errate");
        }

    }

    //mi sposto sull'interfaccia della signup
    public void eventButtonSignUpSignIn(ActionEvent actionEvent) throws IOException {
        textfield_signin_username.setText("");
        passwordfield_signin_password.setText("");
        ClientInterface.switchScene(PageType.SIGN_UP);
    }

    //annullo la signin e torno dove ero
    public void eventButtonCancelSignIn(ActionEvent actionEvent) throws IOException {
        textfield_signin_username.setText("");
        passwordfield_signin_password.setText("");
        ClientInterface.switchScene(PageType.POSTSEARCHINTERFACE);
    }
}
