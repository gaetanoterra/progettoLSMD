import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;

import java.io.IOException;

public class ControllerSignIn {

    ClientServerManager clm = Main.getClientServerManager();

    @FXML
    private Label label_error_message_signin;
    @FXML
    private TextField textfield_signin_username, textfield_signin_password;

    //invio le cose a ClientServerManager, il quale effettuerà la richiesta a ClientManager
    public void eventButtonConfirmSignIn(ActionEvent actionEvent) throws IOException {
        clm.send(new MessageLogin(Opcode.Message_Login, textfield_signin_username.getText(), textfield_signin_password.getText()));
        /*if(controlloUsernamePassword){
            label_error_message_signin.setText("");
            Main.switchScene(3);
            //adesso devo modificare il messaggio comunicando che il signin ha avuto successo
        }
        else{
            Main.switchScene(0);
            label_error_message_signin.setText("Username o password errate");
        }*/

    }

    public void eventButtonSignUpSignIn(ActionEvent actionEvent) {
        Main.switchScene(2);
    }

    public void eventButtonCancelSignIn(ActionEvent actionEvent) {
        Main.switchScene(0);
    }
}
