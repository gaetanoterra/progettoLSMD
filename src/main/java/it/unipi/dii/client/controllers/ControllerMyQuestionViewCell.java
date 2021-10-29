package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Post;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class ControllerMyQuestionViewCell extends ListCell<Post> {

    @javafx.fxml.FXML
    private Label titleLabel;
    @javafx.fxml.FXML
    private Label viewsLabel;
    @javafx.fxml.FXML
    private ImageView deleteBinImageview;

    private FXMLLoader mqvcFXMLLoader;

    @Override
    protected void updateItem(Post post, boolean empty) {

        super.updateItem(post, empty);

        if (empty || post == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (this.mqvcFXMLLoader == null) {
                this.mqvcFXMLLoader = new FXMLLoader(getClass().getResource("/XMLStructures/myQuestionViewCell.fxml"));
                this.mqvcFXMLLoader.setController(this);
                try {
                    this.mqvcFXMLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        this.titleLabel.setText(post.gettitle());
        this.viewsLabel.setText("Answers:\n" + post.getAnswersNumber());
        this.deleteBinImageview.setOnMouseClicked(mouseEvent -> {deleteQuestion();});
    }

    private void deleteQuestion(){}
}
