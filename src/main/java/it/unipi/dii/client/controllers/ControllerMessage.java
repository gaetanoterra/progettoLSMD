package it.unipi.dii.client.controllers;

import it.unipi.dii.client.ClientInterface;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

//classe preposta a controllare l'interfaccia del messaggio
public class ControllerMessage {

    @FXML
    private static TextArea textarea_message;

    public static void setTextArea(String testo){
        textarea_message.setText(testo);
    }

    public void eventButtonCloseMessage(ActionEvent actionEvent) throws IOException {
        textarea_message.setText("");
        ClientInterface.switchScene(PageType.POSTSEARCH_INTERFACE);
    }
}
