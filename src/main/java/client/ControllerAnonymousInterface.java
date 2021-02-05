package client;

import javafx.event.ActionEvent;

import server.*;
import middleware.*;

public class ControllerAnonymousInterface {

    public void eventButtonSignIn(ActionEvent event){
        Main.switchScene(1);
    }

    public void eventButtonSignUp(ActionEvent event){
        Main.switchScene(2);
    }
}
