package it.unipi.dii.client.controllers;

import it.unipi.dii.client.ClientInterface;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

//classe preposta a controllare l'interfaccia del messaggio
public class ControllerMessage {

    @FXML
    private static TextArea textarea_message;

    public static void setTextArea(String testo){
        textarea_message.setText(testo);
    }

    public void eventButtonCloseMessage(ActionEvent actionEvent) {
        textarea_message.setText("");
        ClientInterface.switchScene(PageType.POST_SEARCH_INTERFACE);
    }
}
