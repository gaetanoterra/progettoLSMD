package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessagePost;
import it.unipi.dii.Libraries.Messages.OperationCD;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
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
        tags.replaceAll((str) -> str.strip());
        tags.removeIf((str) -> str.equals(""));

        Post post = new Post();

        post.setAnswers(null)
                .setBody(textarea_body_post.getText())
                .setCreationDate(Instant.now().toEpochMilli())
                .setTitle(textfield_titolo_post.getText())
                .setTags(tags);

        clm.send(new MessagePost(OperationCD.Create, post));

        //ClientInterface.fillProfileInterface(clm.getLoggedUser());
        ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
    }

    public void eventButtonCloseWrite(ActionEvent actionEvent) throws IOException {
        ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
    }
}
