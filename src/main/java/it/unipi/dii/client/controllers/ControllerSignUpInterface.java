package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageSignUp;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.IOException;

//classe preposta a gestire l'interfaccia della registrazione utente
public class ControllerSignUpInterface {

    ServerConnectionManager clientServerManager;
    @FXML
    private TextField userNameTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Label newUserLabel;
    @FXML
    private Button registerButton;
    @FXML
    private GridPane signUpGridPane;
    @FXML
    private TextField webSiteURLTextField;

    public ControllerSignUpInterface(){
        this.clientServerManager = ClientInterface.getServerConnectionManager();
    }

    public void eventButtonRegister(ActionEvent actionEvent) throws IOException, InterruptedException {

    }


}
