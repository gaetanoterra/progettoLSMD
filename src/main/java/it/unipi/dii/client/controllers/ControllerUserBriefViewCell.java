package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageGetUserData;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

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

    public ControllerUserBriefViewCell(PageType pageType, ServerConnectionManager serverConnectionManager) {
        this.pageType = pageType;
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
                this.pbvFXMLLoader = new FXMLLoader(getClass().getResource("/XMLStructures/UserBriefViewCell.fxml"));
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

                this.anchorPaneUser.setOnScroll(mouseEvent -> eventUpdateImage(user));

                setText(null);
                setGraphic(anchorPaneUser);
            }
        }
    }

    private void eventUpdateImage(User user) {
        try {
            imageViewProfileInterfaceBrief.setImage(new Image(user.getProfileImage()));
        }catch (IllegalArgumentException | NullPointerException e){
            imageViewProfileInterfaceBrief.setImage(new Image("/images/anonymous_user.png"));
        }
    }

    public void eventOpenUserProfile(User user) throws IOException {
            serverConnectionManager.send(new MessageGetUserData(user, false, pageType));
    }

}
