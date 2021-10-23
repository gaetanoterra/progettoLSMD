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

public class ControllerSignUpInterface {

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

    public ControllerSignUpInterface(){
        this.clientServerManager = ClientInterface.getServerConnectionManager();
    }
//(String userId, String displayName, String location, String aboutMe, String websiteURL, String pwd){
    @FXML
    public void eventButtonRegister(ActionEvent actionEvent) {
        User signupUser = new User(null,
                userNameTextField.getText(),
                null,
                aboutMeTextArea.getText(),
                webSiteURLTextField.getText(),
                passwordTextField.getText());
        ClientInterface.sendRegistrationRequest(signupUser);
    }

    public void setErroneousUserName(){
      //  userNameTextField.set
    }

    public void handleSignUpResponse(MessageSignUp msgs) {
        Platform.runLater(() -> {
            if(msgs.getStatus().equals(StatusCode.Message_Ok)){
                ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
                ClientInterface.setLog(msgs.getUser());
                ClientInterface.fillProfileInterface(msgs.getUser());
            }
        });
    }
}
