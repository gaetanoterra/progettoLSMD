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
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.scene.image.ImageView;

import java.awt.event.ActionEvent;
import java.io.IOException;

public class ControllerUserBriefViewCell extends ListCell<User> {

    @FXML
    Label label_body;

    @FXML
    AnchorPane anchorPaneUser;

    @FXML
    Label labelDisplayName;

    @FXML
    ImageView imageViewProfileInterfaceBrief;

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
    protected void updateItem(User user, boolean empty) {
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

            this.displayName = user.getDisplayName();
            labelDisplayName.setText(displayName);

            try {
                imageViewProfileInterfaceBrief.setImage(new Image(user.getProfileImage()));
            }catch (IllegalArgumentException iae){
                imageViewProfileInterfaceBrief.setImage(new Image("/images/anonymous_user.png"));
            }finally{
                this.labelDisplayName.setOnMouseClicked(mouseEvent -> {
                    try {
                        eventOpenUserProfile(user);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                this.anchorPaneUser.setOnScroll(mouseEvent -> {
                    eventupdateImage(user);
                });

                setText(null);
                setGraphic(anchorPaneUser);
            }
        }
    }

    public void eventOpenUserProfile(User user) throws IOException {
            serverConnectionManager.send(new MessageGetUserData(user, false, pageType));
    }

}
