package client;

import client.controllers.*;
import Libraries.User;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import server.Server;

import java.io.IOException;
import java.net.InetAddress;


public class ClientInterface extends Application{

    private static User loggedUser;
    private static ClientServerManager clientServerManager;
    private static Scene[] scenes;
    private static Stage mainStage;

    @Override
    public void start (Stage primaryStage) throws Exception{
        initInterfacesList();
        mainStage = primaryStage;
        switchScene(PageType.ANONYMOUS_INTERFACE);
        //creo un'istanza di client.ClientServerManager per connettermi al server.Server

    }

    //rischio loop con la pagina answer e post
    public static void switchScene(PageType idx) throws IOException{
        mainStage.setScene(scenes[idx.ordinal()]);
        mainStage.show();
    }

    private static boolean hasAlreadyBeenLoaded(PageType idx){ return (scenes[idx.ordinal()] != null); }

    private void initInterfacesList() throws IOException  {
        scenes = new Scene[PageType.values().length];

        scenes[PageType.ANONYMOUS_INTERFACE.ordinal()] = new Scene((new FXMLLoader(getClass().getResource("/anonymousInterface.fxml"))).load());
        scenes[PageType.SIGN_IN.ordinal()]             = new Scene((new FXMLLoader(getClass().getResource("/signin.fxml"))).load());
        scenes[PageType.SIGN_UP.ordinal()]             = new Scene((new FXMLLoader(getClass().getResource("/signup.fxml"))).load());
   /*   interfaces[PageType.PROFILE_INTERFACE.ordinal()]    = new FXMLLoader(getClass().getResource("/profileInterface.fxml.fxml"));
        interfaces[PageType.WRITE.ordinal()]                = new FXMLLoader(getClass().getResource("/write.fxml"));
        interfaces[PageType.ANALYSIS_INTERFACE.ordinal()]   = new FXMLLoader(getClass().getResource("/analysisInterface.fxml"));
        interfaces[PageType.MESSAGE.ordinal()]              = new FXMLLoader(getClass().getResource("/message.fxml"));
        interfaces[PageType.READ_POST.ordinal()]            = new FXMLLoader(getClass().getResource("/read_post.fxml"));
        interfaces[PageType.CREATE_ANSWER.ordinal()]        = new FXMLLoader(getClass().getResource("/createAnswerInterface.fxml"));

     */
    }
    public static ClientServerManager getClientServerManager() {
        return clientServerManager;
    }

    public static void setLog(User user){
        loggedUser = user;
    }

    public static void resetLog(){
        loggedUser = null;
    }

    public static User getLog(){ return loggedUser; }

   // public static PageType getLastPageSeen(){ return last_page_seen; }

    public static void main(String[] args) {
        if (args.length != 2){
            System.err.println("Please enter port number and server address");
            return;
        }
        int portNumber;
        String serverIPAddress;
        try {
            portNumber = Integer.parseInt(args[0]);
            serverIPAddress = args[1];
            if (portNumber > 65535 || portNumber < 2000){
                throw new Exception("Port number is not between 2000 and 65535");
            }
            clientServerManager = new ClientServerManager(portNumber, InetAddress.getByName(serverIPAddress));
        }
        catch (NumberFormatException nfe) {
            System.out.println("Port number and/or backlog are not valid integers");
        }
        catch (IOException ioe) {
            System.out.println("A new server socket cannot be allocated. See stack trace for error details");
            ioe.printStackTrace();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        Application.launch(args);
    }
}
