package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessagePost;
import it.unipi.dii.Libraries.Messages.OperationCD;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

//classe per gestire l'interfaccia dove scrivere un nuovo post
public class ControllerWrite {

    ServerConnectionManager clm = ClientInterface.getServerConnectionManager();

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
