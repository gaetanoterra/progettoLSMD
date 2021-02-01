import javafx.event.ActionEvent;
import javafx.scene.control.*;

public class ControllerMessage {

    private static TextArea textarea_message;

    public static void setTextArea(String testo){
        textarea_message.setText(testo);
    }

    public void eventButtonCloseMessage(ActionEvent actionEvent) {
        textarea_message.setText("");
        Main.switchScene(0);
    }
}
