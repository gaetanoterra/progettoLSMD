package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessagePost;
import it.unipi.dii.Libraries.Messages.OperationCD;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class ControllerMyQuestionViewCell extends ListCell<Post> {

    @FXML
    private Label titleLabel;
    @FXML
    private Label viewsLabel;
    @FXML
    private ImageView deleteBinImageview;
    @FXML
    private SplitPane splitPanePost;

    private FXMLLoader mqvcFXMLLoader;
    private PageType lastPage;
    private ServerConnectionManager serverConnectionManager;
    private String postId;
    private String globalPostId;

    public ControllerMyQuestionViewCell(PageType pageType){
        serverConnectionManager = ClientInterface.getServerConnectionManager();
        this.lastPage = pageType;
    }

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

            this.titleLabel.setText(post.getTitle());
            this.postId = post.getMongoPost_id();
            this.globalPostId = post.getGlobalId();

            //this.viewsLabel.setText("Answers:\n" + post.getAnswersNumber());
            this.deleteBinImageview.setOnMouseClicked(mouseEvent -> {
                try {
                    deleteQuestion(post);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            this.titleLabel.setOnMouseClicked(arg0 -> {
                ClientInterface.getFullPostInterface(globalPostId, lastPage);
            });

            setText(null);
            setGraphic(splitPanePost);
        }
    }

    private void deleteQuestion(Post post) throws IOException {serverConnectionManager.send(new MessagePost(OperationCD.Delete, post));}
}
