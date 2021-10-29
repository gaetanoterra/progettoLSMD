package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Post;

import it.unipi.dii.client.ClientInterface;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class ControllerPostBriefViewCell extends ListCell<Post> {
    @FXML
    Label answersNumberTextLabel;

    @FXML
    Label viewsNumberTextLabel;

    @FXML
    Label titleTextLabel;

    @FXML
    HBox tagsListHBox;

    @FXML
    AnchorPane anchorPanePost;

    private FXMLLoader pbvFXMLLoader;
    private String mongoPostID;


    @Override
    protected void updateItem(Post post, boolean empty) {
        super.updateItem(post, empty);

        if(empty || post == null){
            setText(null);
            setGraphic(null);
        }else {
            if(this.pbvFXMLLoader == null){
                this.pbvFXMLLoader = new FXMLLoader(getClass().getResource("/XMLStructures/PostBriefViewCell.fxml"));
                this.pbvFXMLLoader.setController(this);
                try {
                    this.pbvFXMLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            answersNumberTextLabel.setText(post.getAnswersNumber() + "\nAnswers");
            viewsNumberTextLabel.setText(post.getViews() + "\nViews");
            titleTextLabel.setText(post.gettitle());
            this.mongoPostID = post.getPostId();

            this.anchorPanePost.setOnMouseClicked(arg0 -> ClientInterface.getFullPostInterface(mongoPostID));

            if(tagsListHBox.getChildren().isEmpty())
                for(String tag:post.getTags()){tagsListHBox.getChildren().add(new Label(tag));}

            setText(null);
            setGraphic(anchorPanePost);
        }
    }

}
