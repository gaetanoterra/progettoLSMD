package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageGetUserData;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import it.unipi.dii.Libraries.Answer;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

import java.io.IOException;

public class ControllerAnswerCell extends ListCell<Answer> {

    @FXML
    Polygon arrowUpPolygon;

    @FXML
    Polygon arrowDownPolygon;

    @FXML
    Text voteText;

    @FXML
    Text authorText;

    @FXML
    WebView bodyWebView;

    @FXML
    SplitPane answerCellSplitPane;

    @FXML
    private BorderPane votesBoderPane;

    private FXMLLoader answerCellFXMLLoader;
    private PageType currentPageType;
    private ContextMenu contextMenu;

    public ControllerAnswerCell(PageType currentPageType) {
        this.currentPageType = currentPageType;
    }

    @Override
    protected void updateItem(Answer answer, boolean isEmpty) {
        super.updateItem(answer, isEmpty);

        if (isEmpty || answer == null) {
            setText(null);
            setGraphic(null);
        }else{
            answerCellFXMLLoader = new FXMLLoader(getClass().getResource("/XMLStructures/answerCell.fxml"));
            answerCellFXMLLoader.setController(this);
            try {
                this.answerCellFXMLLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            initContextMenu(answer);
            this.answerCellSplitPane.setOnContextMenuRequested(contextMenuEvent ->
                    contextMenu.show(this.answerCellSplitPane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY())
            );
            arrowDownPolygon.setOnMouseClicked(mouseEvent -> ClientInterface.downvoteAnswer(answer));
            arrowUpPolygon.setOnMouseClicked(mouseEvent -> ClientInterface.upvoteAnswer(answer));
            authorText.setText("Author: " + answer.getOwnerUserName());
            if(answer.getBody() != null)
                bodyWebView.getEngine().loadContent(answer.getBody(), "text/html");
            else
                bodyWebView.getEngine().loadContent("<h1> NO BODY! </h1>", "text/html");

            voteText.setText(String.valueOf(answer.getScore()));
            setText(null);
            setGraphic(answerCellSplitPane);
        }

    }

    private void initContextMenu(Answer answer) {
        String answerId = answer.getAnswerId();
        String postId = answer.getPostId();
        String userId = answer.getOwnerUserId();
        this.contextMenu = new ContextMenu();
        final MenuItem item1 = new MenuItem("See answer writer profile");
        item1.setOnAction(actionEvent -> {
            try {
                // se siamo loggati (getLog != null) e siamo sullo stesso utente di quello del post, allora true (vuol dire che siamo sul nostro profilo)
                ClientInterface.getServerConnectionManager().send(new MessageGetUserData(
                        new User().setUserId(userId).setDisplayName(answer.getOwnerUserName()),
                        (ClientInterface.getLog() != null && ClientInterface.getLog().getUserId().equals(userId)),
                        currentPageType
                ));
            } catch (IOException e) {
                System.out.println("Can't find user");
            }
        });
        contextMenu.getItems().addAll(item1);
        // solo se admin o owner
        boolean ownerOrAdmin = (
                ClientInterface.getLog() != null &&
                (
                        ClientInterface.getLog().isAdmin() ||
                        ClientInterface.getLog().getUserId().equals(answer.getOwnerUserId()) ||
                        ClientInterface.getLog().getDisplayName().equals(answer.getOwnerUserName())
                )
        );
        if (ownerOrAdmin) {
            final MenuItem item2 = new MenuItem("Delete answer");
            item2.setOnAction(actionEvent -> {
                System.out.println("Removing answer " + answerId);
                ClientInterface.deleteAnswer(answer);
            });
            contextMenu.getItems().addAll(item2);
        }
    }
}
