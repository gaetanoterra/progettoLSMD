package client.controllers;

import Libraries.Messages.MessagePost;
import Libraries.Messages.OperationCD;
import Libraries.Post;
import client.ClientInterface;
import client.ClientServerManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

//classe per gestire l'interfaccia dove scrivere un nuovo post
public class ControllerWrite {

    ClientServerManager clm = ClientInterface.getClientServerManager();

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

        ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
    }

    public void eventButtonCloseWrite(ActionEvent actionEvent) throws IOException {
        ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
    }
}
