import javafx.event.ActionEvent;
import javafx.scene.control.*;

public class ControllerSignIn {

    private Label label_error_message_signin;

    public void eventButtonConfirmSignIn(ActionEvent actionEvent) {
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

    public void ceventButtonSignUpSignIn(ActionEvent actionEvent) {
        Main.switchScene(2);
    }

    public void eventButtonCancelSignIn(ActionEvent actionEvent) {
        Main.switchScene(0);
    }
}
