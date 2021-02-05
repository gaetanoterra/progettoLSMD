package client;

import javafx.event.ActionEvent;
import javafx.scene.control.*;

import server.*;
import middleware.*;

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
