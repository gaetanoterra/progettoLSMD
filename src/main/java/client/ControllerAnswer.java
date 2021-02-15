package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import middleware.MessageAnswer;
import middleware.OperationCD;
import server.Answer;

import java.awt.*;
import java.io.IOException;

public class ControllerAnswer {

    @FXML
    TextArea textarea_answer;

    public void eventSendAnswer(ActionEvent actionEvent) throws IOException {
        Answer answer = new Answer();

        answer.setBody(textarea_answer.getText());
        //inserire gli altri set di answer
        Main.getClientServerManager().send(new MessageAnswer(OperationCD.Create, answer, ControllerAnonymousInterface.lastPostSeen().getPostId()));
        Main.switchScene(Main.getLastPageSeen());
    }

    public void eventCloseAnswer(ActionEvent actionEvent) {
        Main.switchScene(Main.getLastPageSeen());
    }
}
