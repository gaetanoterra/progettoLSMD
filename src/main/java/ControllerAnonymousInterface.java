import javafx.event.ActionEvent;
import javafx.scene.control.Button;


public class ControllerAnonymousInterface {

    public void eventButtonSignIn(ActionEvent event){
        Main.switchScene(1);
    }

    public void eventButtonSignUp(ActionEvent event){
        Main.switchScene(2);
    }
}
