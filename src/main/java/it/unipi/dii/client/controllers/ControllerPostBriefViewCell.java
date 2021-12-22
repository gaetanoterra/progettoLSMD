package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageGetUserData;
import it.unipi.dii.Libraries.Post;

import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
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
    private PageType currentPageType;
    private ContextMenu contextMenu;

    public ControllerPostBriefViewCell(PageType currentPageType) {
        this.currentPageType = currentPageType;
    }

    @Override
    protected void updateItem(Post post, boolean empty) {
        super.updateItem(post, empty);

        if(empty || post == null) {
            setText(null);
            setGraphic(null);
        }
        else {
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
            titleTextLabel.setText(post.getTitle());

            this.mongoPostID = post.getPostId();
            initContextMenu(post);
            this.anchorPanePost.setOnContextMenuRequested(contextMenuEvent ->
                contextMenu.show(this.anchorPanePost, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY())
            );
            this.anchorPanePost.setOnMouseClicked(mouseEvent -> {
                MouseButton button = mouseEvent.getButton();
                if (button == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    ClientInterface.getFullPostInterface(mongoPostID, currentPageType);
                }
            });

            if(tagsListHBox.getChildren().isEmpty()) {
                for (String tag : post.getTags()) {
                    tagsListHBox.getChildren().add(new Label(tag));
                }
            }

            setText(null);
            setGraphic(anchorPanePost);
        }
    }

    private void initContextMenu(Post post) {
        String postId = post.getPostId();
        String userId = post.getOwnerUserId();
        this.contextMenu = new ContextMenu();
        final MenuItem item1 = new MenuItem("See post writer profile");
        item1.setOnAction(actionEvent -> {
            try {
                // se siamo loggati (getLog != null) e siamo sullo stesso utente di quello del post, allora true (vuol dire che siamo sul nostro profilo)
                ClientInterface.getServerConnectionManager().send(new MessageGetUserData(
                        new User().setUserId(userId).setDisplayName(post.getOwnerUserName()),
                        (ClientInterface.getLog() != null && ClientInterface.getLog().getUserId().equals(userId)),
                        currentPageType
                ));
            } catch (IOException e) {
                System.out.println("Can't find user");
            }
        });
        contextMenu.getItems().addAll(item1);
        // solo se admin
        if (ClientInterface.getLog() != null && ClientInterface.getLog().isAdmin()) {
            final MenuItem item2 = new MenuItem("Delete post");
            item2.setOnAction(actionEvent -> {
                if (ClientInterface.getLog() != null) {
                    if (ClientInterface.getLog().isAdmin()) {
                        //remove post
                        System.out.println("Removing post " + postId);
                        ClientInterface.deletePost(post);
                    }
                    else {
                        System.out.println("Action not permitted for user " + ClientInterface.getLog().getDisplayName());
                    }
                }
                else {
                    System.out.println("Action not permitted for anonymous user");
                }
            });
            contextMenu.getItems().addAll(item2);
        }
    }
}
