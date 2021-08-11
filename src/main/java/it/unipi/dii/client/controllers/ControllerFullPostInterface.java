package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Post;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;

public class ControllerFullPostInterface {
    @FXML
    ImageView askQuestionImageView;

    @FXML
    Label titleLabel;

    @FXML
    WebView questionWebView;
/*
    @FXML
    ListView
*/
    public void resetInterface() {
        titleLabel.setText("");
        askQuestionImageView.setImage(null);
        questionWebView.getEngine().load("");
    }

    public void fillInterface(Post post) {

    }
}
