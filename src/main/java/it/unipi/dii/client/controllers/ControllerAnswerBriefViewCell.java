package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Messages.MessageAnswer;
import it.unipi.dii.Libraries.Messages.OperationCD;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

import java.io.IOException;

public class ControllerAnswerBriefViewCell extends ListCell<Answer> {

    @FXML
    Label label_body;

    @FXML
    AnchorPane anchorPanePost;

    @FXML
    WebView web_view_body;

    @FXML
    Button button_delete_answer;

    private FXMLLoader pbvFXMLLoader;
    private String mongoPostID;
    private String mongoAnswerId;
    private PageType pageType;
    private ServerConnectionManager serverConnectionManager;

    public ControllerAnswerBriefViewCell(PageType postsearchinterface, ServerConnectionManager serverConnectionManager) {
        pageType = postsearchinterface;
        this.serverConnectionManager = serverConnectionManager;
    }

    @Override
    protected void updateItem(Answer answer, boolean empty) {
        super.updateItem(answer, empty);

        if(empty || answer == null){
            setText(null);
            setGraphic(null);
        }else {
            if(this.pbvFXMLLoader == null){
                this.pbvFXMLLoader = new FXMLLoader(getClass().getResource("/XMLStructures/AnswerBriefViewCell.fxml"));
                this.pbvFXMLLoader.setController(this);
                try {
                    this.pbvFXMLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //label_body.setText(post.getBody());
            web_view_body.getEngine().loadContent(answer.getBody());
            this.mongoPostID = answer.getParentPostId();
            this.mongoAnswerId = answer.getAnswerId();
            this.anchorPanePost.setOnMouseClicked(arg0 -> ClientInterface.getFullPostInterface(mongoPostID, pageType));
            this.button_delete_answer.setOnAction(arg0-> {
                try {
                    serverConnectionManager.send(new MessageAnswer(OperationCD.Delete, answer));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            setText(null);
            setGraphic(anchorPanePost);
        }
    }
}
