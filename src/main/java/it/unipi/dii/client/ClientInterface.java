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
import javafx.util.Pair;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClientInterface extends Application{

    private static User loggedUser;
    public final static String DEFAULT_USERNAME = "Anonymous";
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
    private static ControllerExternalUserInterface controllerExternalUserInterface;


    //TODO: necessario un metodo e una variabile che teng conto dell'ultima interfaccia visitata per implementare i back buttons (forse mantenere la pagina all'interno dei vari controller). allo switch della pagina si chiama un metodo init dove si inizializza la lastPageVisited

    @Override
    public void start (Stage primaryStage) throws Exception{
        initScenesArray();
        mainStage = primaryStage;
        switchScene(PageType.POST_SEARCH_INTERFACE);
    }

    //rischio loop con la pagina answer e post
    public static void switchScene(PageType idx){
        Platform.runLater(()->{
            mainStage.setScene(scenes[idx.ordinal()]);
            mainStage.show();
        });

    }


    private void initScenesArray() throws IOException  {
        scenes = new Scene[PageType.values().length];

        FXMLLoader PostSearchInterfaceloader = new FXMLLoader(getClass().getResource("/XMLStructures/PostSearchInterface.fxml"));
        scenes[PageType.POST_SEARCH_INTERFACE.ordinal()] = new Scene(PostSearchInterfaceloader.load());
        controllerPostSearchInterface = PostSearchInterfaceloader.getController();

        FXMLLoader signInInterfaceloader  = new FXMLLoader(getClass().getResource("/XMLStructures/signin.fxml"));
        scenes[PageType.SIGN_IN.ordinal()] = new Scene(signInInterfaceloader.load());
        controllerSignInInterface = signInInterfaceloader.getController();

        FXMLLoader signupInterfaceloader = new FXMLLoader(getClass().getResource("/XMLStructures/registrationInterface.fxml"));
        scenes[PageType.SIGN_UP.ordinal()] = new Scene(signupInterfaceloader.load());
        controllerSignUpInterface = signupInterfaceloader.getController();

        FXMLLoader fullPostInterfaceLoader = new FXMLLoader(getClass().getResource("/XMLStructures/fullPostInterface.fxml"));
        scenes[PageType.FULL_POST.ordinal()] = new Scene(fullPostInterfaceLoader.load());
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

        FXMLLoader externalUserInterfaceLoader = new FXMLLoader(getClass().getResource("/XMLStructures/externalUserInterface.fxml"));
        scenes[PageType.EXTERNAL_PROFILE.ordinal()] = new Scene(externalUserInterfaceLoader.load());
        controllerExternalUserInterface = externalUserInterfaceLoader.getController();

   /*
        interfaces[PageType.ANALYSIS_INTERFACE.ordinal()]   = new FXMLLoader(getClass().getResource("/analysisInterface.fxml"));
     */
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                          ANONYMOUS INTERFACE APIs                                              //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void fillPostSearchInterface(List<Post> postList){
        System.out.println("Updating Full Post interface with " + postList.size() + " posts.");
        controllerPostSearchInterface.resetInterface();
        controllerPostSearchInterface.fillPostPane(postList);
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

    public static void resetPostSearchInterface() {
        controllerPostSearchInterface.setLoggedOutInterface();
    }

    public static void deletePost(Post post) {
        try {
            serverConnectionManager.send(new MessagePost(
                    OperationCD.Delete,
                    post
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                          FULL POST INTERFACE APIs                                              //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void getFullPostInterface(String id, PageType pageType) {
        try {
            serverConnectionManager.send(new MessageGetPostByParameter(Parameter.Id, id));
        } catch (IOException e) {
            e.printStackTrace();
        }
        controllerFullPostInterface.setLastPage(pageType);
        ClientInterface.switchScene(PageType.FULL_POST);
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
            serverConnectionManager.send(new MessageAnswer(OperationCD.Create, answer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void upvoteAnswer(Answer answer) {
        try {
            serverConnectionManager.send(
                    new MessageVote(
                            OperationCD.Create,
                            answer,
                            +1
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void downvoteAnswer(Answer answer) {
        try {
            serverConnectionManager.send(
                    new MessageVote(
                            OperationCD.Create,
                            answer,
                            -1
                    )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAnswer(Answer answer) {
        try {
            serverConnectionManager.send(new MessageAnswer(OperationCD.Delete,answer));
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


    public static void registrationResponseHandler(MessageSignUp msg){
        controllerSignUpInterface.handleRegistrationResponse(msg);
    }

    public static void fillProfileInterface(User u) throws IOException {
        controllerProfileInterface.fillProfileInterface(u);
    }

    public static void fillCorrelatedUsersList(ArrayList<User> users){
        controllerProfileInterface.fillPersonalCorrelatedUsers(users);
    }

    public static void fillPersonalRecommendedUsers(ArrayList<User> users){
        controllerProfileInterface.fillPersonalRecommendedUsers(users);
    }

    public static void fillAnswersUsers(ArrayList<Answer> answers){
        controllerProfileInterface.fillAnswersUsers(answers);
    }

    public static void fillFollowerList(ArrayList<User> followers) {
        controllerProfileInterface.fillFollowerList(followers);
    }

    public static void fillFollowedList(ArrayList<User> followers) {
        controllerProfileInterface.fillFollowedList(followers);
    }

    public static void loginResponseHandler(MessageLogin msg){
        controllerSignInInterface.handleLogInResponse(msg);
    }

    public static void logoutResponseHandler(){
        controllerProfileInterface.handleLogOutResponse();
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

    public static void initAnalyticsInterface(PageType pageType) throws IOException {
        controllerAnalysisInterface.initAnalyticsInterface(pageType);
    }

    public static void fillMPTagChart(Map<String, Integer> tags){
        controllerAnalysisInterface.resetTagList();
        if(tags != null)
            controllerAnalysisInterface.fillTagList(tags);
        //TODO: chiamare un metodo della controllerAnalysisInterface che riempia la piechart
    }


    public static void fillUserRanking(User[] users){
        controllerAnalysisInterface.resetMPUsersList();
        if(users != null)
            controllerAnalysisInterface.fillMPUsersList(users);
    }

    public static void fillExpertsByTag(String[] users){
        controllerAnalysisInterface.resetExpertUsersList();
        if(users != null)
            controllerAnalysisInterface.fillExpertUsersList(users);
    }

    public static void fillHotTopicsUsers(HashMap<User, ArrayList<Pair<Post, Integer>>> map){
        controllerAnalysisInterface.resetHotTopicsMap();
        controllerAnalysisInterface.fillHotTopicsMap(map);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                                                //
    //                                         EXTERNAL PROFILE APIs                                                  //
    //                                                                                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public static void loadExternalProfile(User user, PageType lastPageVisited) throws IOException {
        controllerExternalUserInterface.initialize(user, lastPageVisited);
        Platform.runLater(() -> ClientInterface.switchScene(PageType.EXTERNAL_PROFILE));
    }

    public static void fillUserPostInterface(ArrayList<Post> posts, String username){
        if (loggedUser != null) {
            if (loggedUser.getDisplayName().equals(username))
                fillPersonalUserPostInterface(posts);
            else
                fillExternalUserPostInterface(posts);
        }
        else
            fillExternalUserPostInterface(posts);
    }

    public static void fillExternalUserPostInterface(ArrayList<Post> posts) {
        Platform.runLater(() ->
                controllerExternalUserInterface.fillExternalUserPosts(posts)
        );
    }
    private static void fillPersonalUserPostInterface(ArrayList<Post> posts) {
        controllerProfileInterface.fillPersonalUserPostInterface(posts);
    }

    public static void deleteUser(User user) {
        try {
            serverConnectionManager.send(new MessageUser(
                    OperationCD.Delete,
                    user
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setUnfollowUser() {
        controllerExternalUserInterface.setUnfollowUser();
    }

    public static void setFollowUser() {
        controllerExternalUserInterface.setFollowUser();
    }

    public static void setFollowUnfollowUser(User user) {
        controllerExternalUserInterface.setFollowUnfollowUser(user);
    }
}

