package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageSignUp;
import it.unipi.dii.Libraries.Messages.StatusCode;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.io.IOException;

public class ControllerRegistrationInterface {

    ServerConnectionManager clientServerManager;

    @FXML
    private PasswordField passwordTextField;
    @FXML
    private GridPane signUpGridPane;
    @FXML
    private TextField webSiteURLTextField;
    @FXML
    private TextField eMailTextField;
    @FXML
    private TextArea aboutMeTextArea;
    @FXML
    private TextField userNameTextField;
    @FXML
    private Label errorRegistrationLabel;

    public ControllerRegistrationInterface(){
        this.clientServerManager = ClientInterface.getServerConnectionManager();
    }

    @FXML
    public void eventButtonRegister(ActionEvent actionEvent) {
        ClientInterface.sendRegistrationRequest(
                new User(null,
                        userNameTextField.getText(),
                        null,
                        aboutMeTextArea.getText(),
                        webSiteURLTextField.getText(),
                        passwordTextField.getText())
        );
    }

    public void handleRegistrationResponse(MessageSignUp msg) {
        Platform.runLater(() -> {
            if (msg.getStatus().equals(StatusCode.Message_Ok)){
                clientServerManager.setLoggedUser(msg.getUser());
                ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
                ClientInterface.setLog(msg.getUser());
                try {
                    ClientInterface.fillProfileInterface(msg.getUser());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ClientInterface.updatePostSearchInterfaceWithLoggedUserInfos(msg.getUser());
                errorRegistrationLabel.setText("");
            }
            else {
                System.out.println("Registration failed");
                errorRegistrationLabel.setText("Username already present");
                errorRegistrationLabel.setTextFill(Color.web("#0076a3"));
            }
        });
    }

    public void setErroneousUserName(){
      //  userNameTextField.set
    }

    public void eventAnnnul(ActionEvent actionEvent) {
        ClientInterface.switchScene(PageType.POST_SEARCH_INTERFACE);
    }

    public void eventRegistrationBack(ActionEvent actionEvent) {
        ClientInterface.switchScene(PageType.POST_SEARCH_INTERFACE);
    }
}
