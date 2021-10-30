package it.unipi.dii.client;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Messages.*;
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
import java.util.Map;


public class ClientInterface extends Application{

    private static User loggedUser;
    private static ServerConnectionManager serverConnectionManager;
    private static Scene[] scenes;
    private static Stage mainStage;

    private static ControllerPostSearchInterface controllerPostSearchInterface;
    private static ControllerSignInInterface controllerSignInInterface;
    private static ControllerRegistrationInterface controllerSignUpInterface;
    private static ControllerFullPostInterface controllerFullPostInterface;
    private static ControllerProfileInterface controllerProfileInterface;
    private static ControllerAnalysisInterface controllerAnalysisInterface;
    private static ControllerWrite controllerWriteInterface;


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

        FXMLLoader signupInterfaceloader = new FXMLLoader(getClass().getResource("/XMLStructures/registrationInterface.fxml"));
        scenes[PageType.SIGN_UP.ordinal()] = new Scene(signupInterfaceloader.load());
        controllerSignUpInterface = signupInterfaceloader.getController();

        FXMLLoader fullPostInterfaceLoader = new FXMLLoader(getClass().getResource("/XMLStructures/fullPostInterface.fxml"));
        scenes[PageType.FULLPOST.ordinal()] = new Scene(fullPostInterfaceLoader.load());
        controllerFullPostInterface = fullPostInterfaceLoader.getController();

        FXMLLoader profileInterfaceLoader = new FXMLLoader(getClass().getResource("/XMLStructures/profileInterface.fxml"));
        scenes[PageType.PROFILE_INTERFACE.ordinal()] = new Scene(profileInterfaceLoader.load());
        controllerProfileInterface = profileInterfaceLoader.getController();

        FXMLLoader analysisInterfaceLoader = new FXMLLoader(getClass().getResource("/XMLStructures/analysisInterface.fxml"));
        scenes[PageType.ANALYSIS_INTERFACE.ordinal()] = new Scene(analysisInterfaceLoader.load());
        controllerAnalysisInterface = analysisInterfaceLoader.getController();


        FXMLLoader writeInterfaceLoader = new FXMLLoader(getClass().getResource("/XMLStructures/write.fxml"));
        scenes[PageType.WRITE.ordinal()] = new Scene(writeInterfaceLoader.load());
        controllerWriteInterface = writeInterfaceLoader.getController();

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
        System.out.println("Updating Full Post interface with " + postArrayList.size() + " posts.");
        controllerPostSearchInterface.resetInterface();
        controllerPostSearchInterface.fillPostPane(postArrayList);
    }

    public static ServerConnectionManager getServerConnectionManager() {
        return serverConnectionManager;
    }

    public static void setLog(User user){
        loggedUser = user;
    }

    public static void resetLog(){
        setLog(null);
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

    public static void postNewAnswer(String postId, String body) {
        Answer answer = new Answer(body);
        try {
            serverConnectionManager.send(new MessageAnswer(OperationCD.Create, answer, postId));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void upvoteAnswer(String postId, String answerId) {
        try {
            serverConnectionManager.send(
                    new MessageVote(
                            OperationCD.Create,
                            new Answer("").setAnswerId(answerId).setPostId(postId),
                            +1
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void downvoteAnswer(String postId, String answerId) {
        try {
            serverConnectionManager.send(
                    new MessageVote(
                            OperationCD.Create,
                            new Answer("").setAnswerId(answerId).setPostId(postId),
                            -1
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                switchScene(PageType.POSTSEARCHINTERFACE);
                controllerPostSearchInterface.setLoggedInterface(u.getDisplayName(),u.getProfileImage());
            }
        });
    }
    public static void fillProfileInterface(User u) {
        controllerProfileInterface.fillProfileInterface(u);
    }

    public static void loginResponseHandler(MessageLogin msg){
        controllerSignInInterface.handleLogInResponse(msg);
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
                throw new RuntimeException("Port number is not valid, it must be between 2000 and 65535");
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                         ANALYSIS INTERFACE APIs                                                //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void fillMPTagChart(Map<String, Integer> tags){
        controllerAnalysisInterface.resetTagList();
        controllerAnalysisInterface.fillTagList(tags);
        //TODO: chiamare un metodo della controllerAnalysisInterface che riempia la piechart
    }

    public static void fillMPTagLocationChart(String[] tags){
        controllerAnalysisInterface.resetTagLocationList();
        controllerAnalysisInterface.fillTagLocationList(tags);
        //TODO: chiamare un metodo della controllerAnalysisInterface che riempia la piechart
    }
}

