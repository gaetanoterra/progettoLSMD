package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.*;
import it.unipi.dii.Libraries.Post;

import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;

import java.io.IOException;

public class ControllerUserBriefViewCell extends ListCell<String> {

    @FXML
    Label label_body;

    @FXML
    AnchorPane anchorPaneUser;

    @FXML
    WebView web_view_displayname;

    @FXML
    Button button_follow;

    private FXMLLoader pbvFXMLLoader;
    private String displayName;
    private PageType pageType;
    private ServerConnectionManager serverConnectionManager;
    private boolean followed;

    public ControllerUserBriefViewCell(PageType postsearchinterface, ServerConnectionManager serverConnectionManager) {
        pageType = postsearchinterface;
        this.serverConnectionManager = serverConnectionManager;
    }

    @Override
    protected void updateItem(String user, boolean empty) {
        super.updateItem(user, empty);

        if(empty || user == null){
            setText(null);
            setGraphic(null);
        }else {
            if(this.pbvFXMLLoader == null){
                this.pbvFXMLLoader = new FXMLLoader(getClass().getResource("/XMLStructures/userBriefViewCell.fxml"));
                this.pbvFXMLLoader.setController(this);
                try {
                    this.pbvFXMLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            web_view_displayname.getEngine().loadContent(user);
            this.displayName = user;

            User u = new User(null, displayName, null, null, null, null);
            MessageGetUserData messageGetUserData = new MessageGetUserData(u, false, pageType);

            this.anchorPaneUser.setOnMouseClicked(arg0 -> {
                try {
                    serverConnectionManager.send(messageGetUserData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            setText(null);
            setGraphic(anchorPaneUser);
        }
    }
}
