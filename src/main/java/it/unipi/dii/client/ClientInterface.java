package it.unipi.dii.client;

import it.unipi.dii.Libraries.Messages.MessageGetPostByParameter;
import it.unipi.dii.Libraries.Messages.MessageSignUp;
import it.unipi.dii.Libraries.Messages.Parameter;
import it.unipi.dii.Libraries.Messages.StatusCode;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.Libraries.Post;

import it.unipi.dii.client.controllers.*;
import javafx.application.Application;
import javafx.application.Platform;
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

    private static ControllerPostSearchInterface controllerPostSearchInterface;
    private static ControllerSignInInterface controllerSignInInterface;
    private static ControllerSignUpInterface controllerSignUpInterface;
    private static ControllerFullPostInterface controllerFullPostInterface;
    private static ControllerProfileInterface controllerProfileInterface;

    @Override
    public void start (Stage primaryStage) throws Exception{
        initScenesArray();
        mainStage = primaryStage;
        switchScene(PageType.POSTSEARCHINTERFACE);
    }

    //rischio loop con la pagina answer e post
    public static void switchScene(PageType idx){
        mainStage.setScene(scenes[idx.ordinal()]);
        mainStage.show();
    }


    private void initScenesArray() throws IOException  {
        scenes = new Scene[PageType.values().length];

        FXMLLoader PostSearchInterfaceloader = new FXMLLoader(getClass().getResource("/XMLStructures/PostSearchInterface.fxml"));
        scenes[PageType.POSTSEARCHINTERFACE.ordinal()] = new Scene(PostSearchInterfaceloader.load());
        controllerPostSearchInterface = PostSearchInterfaceloader.getController();

        FXMLLoader signInInterfaceloader  = new FXMLLoader(getClass().getResource("/XMLStructures/signin.fxml"));
        scenes[PageType.SIGN_IN.ordinal()] = new Scene(signInInterfaceloader.load());
        controllerSignInInterface = signInInterfaceloader.getController();

        FXMLLoader signupInterfaceloader = new FXMLLoader(getClass().getResource("/XMLStructures/signup.fxml"));
        scenes[PageType.SIGN_UP.ordinal()] = new Scene(signupInterfaceloader.load());
        controllerSignUpInterface = signupInterfaceloader.getController();

        FXMLLoader fullPostInterfaceLoader = new FXMLLoader(getClass().getResource("/XMLStructures/fullPostInterface.fxml"));
        scenes[PageType.FULLPOST.ordinal()] = new Scene(fullPostInterfaceLoader.load());
        controllerFullPostInterface = fullPostInterfaceLoader.getController();

        FXMLLoader profileInterfaceLoader = new FXMLLoader(getClass().getResource("/XMLStructures/profileInterface.fxml"));
        scenes[PageType.PROFILE_INTERFACE.ordinal()] = new Scene(profileInterfaceLoader.load());
        controllerProfileInterface = profileInterfaceLoader.getController();

   /*
        interfaces[PageType.ANALYSIS_INTERFACE.ordinal()]   = new FXMLLoader(getClass().getResource("/analysisInterface.fxml"));
     */
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                          ANONYMOUS INTERFACE APIs                                              //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void fillPostSearchInterface(ArrayList<Post> postArrayList){
        controllerPostSearchInterface.resetInterface();
        controllerPostSearchInterface.fillPostPane(postArrayList);
    }

    public static ServerConnectionManager getServerConnectionManager() {
        return serverConnectionManager;
    }

    public static void resetLog(){
        loggedUser = null;
    }

    public static User getLog(){ return loggedUser; }

    public static void updatePostSearchInterfaceWithLoggedUserInfos(User u) {
        controllerPostSearchInterface.setLoggedInterface(u.getDisplayName(), u.getProfileImage());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                          FULL POST INTERFACE APIs                                              //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void getFullPostInterface(String id) {
        try {
            serverConnectionManager.send(new MessageGetPostByParameter(Parameter.Id, id));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ClientInterface.switchScene(PageType.FULLPOST);
    }

    public static void fillFullPostInterface(Post post){
        Platform.runLater(() -> {
            controllerFullPostInterface.resetInterface();
            controllerFullPostInterface.fillInterface(post);
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                          PROFILE INTERFACE APIs                                                //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void sendRegistrationRequest(User u){
        try {
            serverConnectionManager.send(new MessageSignUp(u));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void registrationResponseHandler(User u, StatusCode stc){
        Platform.runLater(() -> {
            if(stc.equals(StatusCode.Message_Ok)){
                switchScene(PageType.PROFILE_INTERFACE);
                controllerProfileInterface.fillProfileInterface(u);
            }
        });
    }


    public static void main(String[] args) {

        if (args.length != 2){
            System.err.println("Please enter port number and server address");
            return;
        }

        int portNumber;
        String serverIPAddress;
        try {
            portNumber = Integer.parseInt(args[1]);
            serverIPAddress = args[0];
            if (portNumber > 65535 || portNumber < 2000){
                throw new RuntimeException("Port number is not between 2000 and 65535");
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
        catch(RuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        Application.launch(args);
    }
}

