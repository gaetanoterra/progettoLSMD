package client;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import server.*;
import middleware.*;

public class Main extends Application{

    private static User logged_user = null;

    private static ClientServerManager clientServerManager;
    private static ControllerAnonymousInterface controllerAnonymousInterface;
    private static ControllerProfileInterface controllerProfileInterface;
    private static AnchorPane root;

    private static List<AnchorPane> grid = new ArrayList<AnchorPane>();

    private static PageType idx_cur = PageType.ANONYMOUS_INTERFACE, last_page_seen = PageType.ANONYMOUS_INTERFACE;

    @Override
    public void start (Stage primaryStage) throws Exception {
        try {

            root = FXMLLoader.load(getClass().getResource("/anchor.fxml"));

            grid.add(FXMLLoader.load(getClass().getResource("/anonymousInterface.fxml")));
            grid.add(FXMLLoader.load(getClass().getResource("/signin.fxml")));
            grid.add(FXMLLoader.load(getClass().getResource("/signup.fxml")));
            // grid.add(FXMLLoader.load(getClass().getResource("/profileInterface.fxml")));
            grid.add(FXMLLoader.load(getClass().getResource("/write.fxml")));
            grid.add( FXMLLoader.load(getClass().getResource("/analysisInterface.fxml")));
            grid.add( FXMLLoader.load(getClass().getResource("/message.fxml")));
            grid.add( FXMLLoader.load(getClass().getResource("/post.fxml")));
            grid.add( FXMLLoader.load(getClass().getResource("/answer.fxml")));

            root.getChildren().add(grid.get(PageType.ANONYMOUS_INTERFACE.ordinal()));
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.show();
        }catch(Exception e){e.printStackTrace();}

        //creo un'istanza di client.ClientServerManager per connettermi al server.Server
        clientServerManager = new ClientServerManager(8080, InetAddress.getLocalHost());
        clientServerManager.run();

        controllerAnonymousInterface = new ControllerAnonymousInterface(clientServerManager);
        controllerProfileInterface = new ControllerProfileInterface(clientServerManager);
    }

    //rischio loop con la pagina answer e post
    public static void switchScene(PageType idx){

        root.getChildren().remove(grid.get(idx_cur.ordinal()));
        root.getChildren().add(grid.get(idx.ordinal()));
        last_page_seen = idx_cur;
        idx_cur = idx;
    }

    public static ClientServerManager getClientServerManager() {
        return clientServerManager;
    }

    public static void setLog(User user){
        logged_user = user;
    }

    public static void resetLog(){
        logged_user = null;
    }

    public static User getLog(){ return logged_user; }

    public static PageType getLastPageSeen(){ return last_page_seen; }

    public static ControllerAnonymousInterface getControllerAnonymousInterface() { return controllerAnonymousInterface; }

    public static ControllerProfileInterface getControllerProfileInterface() { return controllerProfileInterface; }

    public static void main(String[] args) {
        launch(args);
    }
}
