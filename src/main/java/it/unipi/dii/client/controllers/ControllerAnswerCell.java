package it.unipi.dii.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import it.unipi.dii.Libraries.Answer;
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

            //TODO set the action increase up/downVotes for the arrows

            //arrowDownPolygon.setOnMouseClicked();
            //arrowUpPolygon.setOnMouseClicked();
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
}
