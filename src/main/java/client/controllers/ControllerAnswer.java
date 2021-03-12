package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import middleware.MessageAnswer;
import middleware.OperationCD;
import server.Answer;

import java.io.IOException;

public class ControllerAnswer {

    @FXML
    TextArea textarea_answer;

    public void eventSendAnswer(ActionEvent actionEvent) throws IOException {
        Answer answer = new Answer();

        answer.setBody(textarea_answer.getText());
        //inserire gli altri set di answer
        ClientInterface.getClientServerManager().send(new MessageAnswer(OperationCD.Create, answer, ControllerAnonymousInterface.lastPostSeen().getPostId()));
        ClientInterface.switchScene(ClientInterface.getLastPageSeen());
    }

    public void eventCloseAnswer(ActionEvent actionEvent) {
        ClientInterface.switchScene(ClientInterface.getLastPageSeen());
    }
}
