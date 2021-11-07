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
//(String userId, String displayName, String location, String aboutMe, String websiteURL, String pwd){
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
                ClientInterface.fillProfileInterface(msg.getUser());
                ClientInterface.updatePostSearchInterfaceWithLoggedUserInfos(msg.getUser());
            }
            else {
                System.out.println("Failed registration");
            }
        });
    }

    public void setErroneousUserName(){
      //  userNameTextField.set
    }
}
