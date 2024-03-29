package it.unipi.dii.client.controllers;

import it.unipi.dii.Libraries.Messages.MessageLogin;
import it.unipi.dii.Libraries.Messages.StatusCode;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.ClientInterface;
import it.unipi.dii.client.ServerConnectionManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;

//classe preposta a gestire l'interfaccia del login
public class ControllerSignInInterface {

    ServerConnectionManager serverConnectionManager;

    @FXML
    private Label errorMessageSignInLabel;
    @FXML
    private TextField textfieldSignInUsername;
    @FXML
    private PasswordField signInPasswordField;

    public ControllerSignInInterface(){
        this.serverConnectionManager = ClientInterface.getServerConnectionManager();
    }

    //invio opcode, username e password a client.ClientServerManager, il quale effettuerà la richiesta a server.ClientManager
    @FXML
    public void eventButtonConfirmSignIn(ActionEvent actionEvent) throws IOException {
        serverConnectionManager.send(new MessageLogin(new User()
                                                            .setPassword(signInPasswordField.getText())
                                                            .setDisplayName(textfieldSignInUsername.getText())));

    }

    //mi sposto sull'interfaccia della signup
    @FXML
    public void eventButtonSignUpSignIn(ActionEvent actionEvent){
        ClientInterface.switchScene(PageType.SIGN_UP);
    }

    //annullo la signin e torno dove ero
    public void handleLogInResponse(MessageLogin messageLogin) {
        Platform.runLater(() -> {
            if (messageLogin.getStatus().equals(StatusCode.Message_Ok)){
                serverConnectionManager.setLoggedUser(messageLogin.getUser());
                ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
                ClientInterface.setLog(messageLogin.getUser());
                try {
                    ClientInterface.fillProfileInterface(messageLogin.getUser());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ClientInterface.updatePostSearchInterfaceWithLoggedUserInfos(messageLogin.getUser());
                errorMessageSignInLabel.setText("");
            }
            else {
                errorMessageSignInLabel.setText("Username or password not valid");
                errorMessageSignInLabel.setTextFill(Color.web("#0076a3"));
            }
        });
    }


    public void eventBack(ActionEvent actionEvent) {
        ClientInterface.switchScene(PageType.POST_SEARCH_INTERFACE);
    }
}
