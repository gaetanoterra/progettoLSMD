package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class ControllerFriendOfFriendsViewCell extends ListCell<User> {
    @javafx.fxml.FXML
    private Label friendsInCommonLabel;

    @javafx.fxml.FXML
    private Label usernameLabel;

    @javafx.fxml.FXML
    private ImageView userImageView;

    @javafx.fxml.FXML
    private Button followButton;

    private FXMLLoader fofFXMLLoader;


    @Override
    protected void updateItem(User user, boolean empty) {

        super.updateItem(user, empty);

        if (empty || user == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (this.fofFXMLLoader == null) {
                this.fofFXMLLoader = new FXMLLoader(getClass().getResource("/XMLStructures/myQuestionViewCell.fxml"));
                this.fofFXMLLoader.setController(this);
                try {
                    this.fofFXMLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
