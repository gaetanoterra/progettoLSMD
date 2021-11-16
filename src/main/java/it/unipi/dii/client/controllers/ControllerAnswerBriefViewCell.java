package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Post;

import it.unipi.dii.client.ClientInterface;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;

import java.io.IOException;

public class ControllerAnswerBriefViewCell extends ListCell<Post> {

    @FXML
    Label label_body;

    @FXML
    AnchorPane anchorPanePost;

    @FXML
    WebView web_view_body;

    private FXMLLoader pbvFXMLLoader;
    private String mongoPostID;
    private PageType pageType;

    public ControllerAnswerBriefViewCell(PageType postsearchinterface) {
        pageType = postsearchinterface;
    }

    @Override
    protected void updateItem(Post post, boolean empty) {
        super.updateItem(post, empty);

        if(empty || post == null){
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
            web_view_body.getEngine().loadContent(post.getBody());
            this.mongoPostID = post.getPostId();
            this.anchorPanePost.setOnMouseClicked(arg0 -> ClientInterface.getFullPostInterface(mongoPostID, pageType));

            setText(null);
            setGraphic(anchorPanePost);
        }
    }
}
