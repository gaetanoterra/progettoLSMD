package client;

import javafx.event.ActionEvent;
import javafx.scene.control.*;

//classe preposta a controllare l'interfaccia del messaggio
public class ControllerMessage {

    private static TextArea textarea_message;

    public static void setTextArea(String testo){
        textarea_message.setText(testo);
    }

    public void eventButtonCloseMessage(ActionEvent actionEvent) {
        textarea_message.setText("");
        ClientInterface.switchScene(PageType.ANONYMOUS_INTERFACE);
    }
}
