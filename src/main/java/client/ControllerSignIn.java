package client;

import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;

import middleware.*;
import server.*;

import java.io.IOException;

public class ControllerSignIn {

    ClientServerManager clm = Main.getClientServerManager();

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

        clm.send(new MessageLogin(Opcode.Message_Login, user));
        if(clm.checkLastServerAnswer()){
            label_error_message_signin.setText("");

            //mi serve una funzione che popola l'interfaccia del profilo utente con i dati dell'utente
            // (utente qualsiasi, in questo caso sarà dell'utente loggato ma se clicco sul profilo di un altro utente lo visualizzo nellos tesso modo)

            //switch della pagina
            Main.switchScene(3);
        }
        else{
            label_error_message_signin.setText("*Username o password errate");
        }

    }

    public void eventButtonSignUpSignIn(ActionEvent actionEvent) {
        textfield_signin_username.setText("");
        passwordfield_signin_password.setText("");
        Main.switchScene(2);
    }

    public void eventButtonCancelSignIn(ActionEvent actionEvent) {
        textfield_signin_username.setText("");
        passwordfield_signin_password.setText("");
        Main.switchScene(0);
    }
}
