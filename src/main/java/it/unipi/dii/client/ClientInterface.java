package it.unipi.dii.client;

import it.unipi.dii.Libraries.User;
import it.unipi.dii.Libraries.Post;

import it.unipi.dii.client.controllers.ControllerAnonymousInterface;
import it.unipi.dii.client.controllers.ControllerSignInInterface;
import it.unipi.dii.client.controllers.ControllerSignUpInterface;
import it.unipi.dii.client.controllers.PageType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;


public class ClientInterface extends Application{

    private static User loggedUser;
    private static ServerConnectionManager serverConnectionManager;
    private static Scene[] scenes;
    private static Stage mainStage;
    private static ControllerAnonymousInterface controllerAnonymousInterface;
    private static ControllerSignInInterface controllerSignInInterface;
    private static ControllerSignUpInterface controllerSignUpInterface;

    @Override
    public void start (Stage primaryStage) throws Exception{
        initScenesArray();
        mainStage = primaryStage;
        switchScene(PageType.ANONYMOUS_INTERFACE);
    }

    //rischio loop con la pagina answer e post
    public static void switchScene(PageType idx){
        mainStage.setScene(scenes[idx.ordinal()]);
        mainStage.show();
    }


    private void initScenesArray() throws IOException  {
        scenes = new Scene[PageType.values().length];

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/XMLStructures/anonymousInterface.fxml"));
        scenes[PageType.ANONYMOUS_INTERFACE.ordinal()] = new Scene(loader.load());
        controllerAnonymousInterface = loader.getController();

        loader = new FXMLLoader(getClass().getResource("/XMLStructures/signin.fxml"));
        scenes[PageType.SIGN_IN.ordinal()] = new Scene(loader.load());
        controllerSignInInterface = loader.getController();

        loader = new FXMLLoader(getClass().getResource("/XMLStructures/signup.fxml"));
        scenes[PageType.SIGN_UP.ordinal()] = new Scene(loader.load());
        controllerSignUpInterface = loader.getController();
        
   /*   interfaces[PageType.PROFILE_INTERFACE.ordinal()]    = new FXMLLoader(getClass().getResource("/profileInterface.fxml.fxml"));
        interfaces[PageType.WRITE.ordinal()]                = new FXMLLoader(getClass().getResource("/write.fxml"));
        interfaces[PageType.ANALYSIS_INTERFACE.ordinal()]   = new FXMLLoader(getClass().getResource("/analysisInterface.fxml"));
        interfaces[PageType.MESSAGE.ordinal()]              = new FXMLLoader(getClass().getResource("/message.fxml"));
        interfaces[PageType.READ_POST.ordinal()]            = new FXMLLoader(getClass().getResource("/read_post.fxml"));
        interfaces[PageType.CREATE_ANSWER.ordinal()]        = new FXMLLoader(getClass().getResource("/createAnswerInterface.fxml"));

     */
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                          ANONYMOUS INTERFACE APIs                                              //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void fillAnonymousInterfacePostsPanel(ArrayList<Post> postArrayList){
        controllerAnonymousInterface.resetInterface();
        controllerAnonymousInterface.fillPostPane(postArrayList);
    }


    public static ServerConnectionManager getServerConnectionManager() {
        return serverConnectionManager;
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
            serverConnectionManager = new ServerConnectionManager(portNumber, InetAddress.getByName(serverIPAddress));
            serverConnectionManager.setDaemon(true);
            serverConnectionManager.start();
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
