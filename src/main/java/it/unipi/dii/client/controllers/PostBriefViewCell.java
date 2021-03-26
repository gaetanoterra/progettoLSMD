package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Post;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class PostBriefViewCell  extends ListCell<Post> {
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
            answersNumberTextLabel.setText(post.getAnswers().size() + "\nAnswers");
            viewsNumberTextLabel.setText(post.getViews() + "\nViews");
            titleTextLabel.setText(post.gettitle());
            for(String tag:post.getTags()){tagsListHBox.getChildren().add(new Label(tag));}

            setText(null);
            setGraphic(anchorPanePost);
        }
    }
}
