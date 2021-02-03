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

    //invio opcode, username e password a ClientServerManager, il quale effettuerà la richiesta a ClientManager
    public void eventButtonConfirmSignIn(ActionEvent actionEvent) throws IOException, InterruptedException {
        clm.send(new MessageLogin(Opcode.Message_Login, textfield_signin_username.getText(), textfield_signin_password.getText()));
        if(clm.controlloUsernamePassword()){
            label_error_message_signin.setText("");
            Main.switchScene(3);
        }
        else{
            label_error_message_signin.setText("*Username o password errate");
        }

    }

    public void eventButtonSignUpSignIn(ActionEvent actionEvent) {
        Main.switchScene(2);
    }

    public void eventButtonCancelSignIn(ActionEvent actionEvent) {
        Main.switchScene(0);
    }
}
