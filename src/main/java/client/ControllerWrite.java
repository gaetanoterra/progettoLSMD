package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import server.*;
import middleware.*;

//classe per gestire l'interfaccia dove scrivere un nuovo post
public class ControllerWrite {

    ClientServerManager clm = Main.getClientServerManager();

    @FXML
    private TextField textfield_titolo_post;
    @FXML
    private TextArea textarea_body_post;
    @FXML
    private TextField textfield_tags_post;

    public void eventButtonSend(ActionEvent actionEvent) throws IOException {
        //invio il post
        List<String> tags = Arrays.asList(textfield_tags_post.getText().split(";"));

        Post post = new Post();

        //post.setPostId("1234");
        post.setAnswers(null);
        post.setBody(textarea_body_post.getText());
        post.setCreationDate(new Date());
        post.setTitle(textfield_titolo_post.getText());
        post.setTags(tags);

        clm.send(new MessagePost(OperationCD.Create, post));

        Main.switchScene(PageType.PROFILE_INTERFACE);
    }

    public void eventButtonCloseWrite(ActionEvent actionEvent) {
        Main.switchScene(PageType.PROFILE_INTERFACE);
    }
}
