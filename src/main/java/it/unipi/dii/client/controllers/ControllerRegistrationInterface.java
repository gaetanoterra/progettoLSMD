package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageSignUp;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

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

    public void setErroneousUserName(){
      //  userNameTextField.set
    }
}
