import javafx.event.ActionEvent;

public class ControllerSignUp {
    public void eventButtonConfirmSignUp(ActionEvent actionEvent) {
        ControllerMessage.setTextArea("Registrazione avvenuta con successo!");
        Main.switchScene(4);
    }

    public void eventButtonCloseSignup(ActionEvent actionEvent) {
        Main.switchScene(0);
    }
}
