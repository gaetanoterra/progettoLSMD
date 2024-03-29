package it.unipi.dii.client;

import it.unipi.dii.Libraries.Messages.*;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.controllers.PageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

//classe preposta a ricevere le risposte dal server e passare i risultati ai vari Controller
public class ServerConnectionManager extends Thread {

    private ObjectOutputStream messageOutputStream;
    private ObjectInputStream messageInputStream;
    private Socket clientSocket;
    private User loggedUser;

    public ServerConnectionManager(int porta, InetAddress in) throws IOException{
        clientSocket = new Socket(in, porta);
        messageOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        messageInputStream = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void run(){

        try {
            System.out.println("Requested connection to server");

            while(true){
                Message message = receive();
                System.out.println("Received message with opCode: " + message.getOpcode());

                switch (message.getOpcode()){

                    case Message_Login:
                        MessageLogin msgl = (MessageLogin) message;

                        //se ricevo l'utente devo chiamare una funzione che inserisca i dati dell'utente nell'interfaccia
                        ClientInterface.loginResponseHandler(msgl);
                        break;

                    case Message_Logout:
                        ClientInterface.logoutResponseHandler();
                        // ClientInterface.getControllerAnonymousInterface().resetInterface();
                        break;

                    case Message_Signup:
                        /*
                        MessageSignUp messageSignUp = (MessageSignUp) message;
                        ClientInterface.registrationResponseHandler(messageSignUp.getUser(), messageSignUp.getStatus());
                        */
                        MessageSignUp messageSignUp = (MessageSignUp)message;
                        ClientInterface.registrationResponseHandler(messageSignUp);
                        break;

                    case Message_User:
                        MessageUser messageUser = (MessageUser) message;
                        switch (messageUser.getOperation()){
                            case Create -> {}
                            case Delete -> {if (messageUser.getUser() == null){
                                                ClientInterface.resetPostSearchInterface();
                                                ClientInterface.switchScene(PageType.POST_SEARCH_INTERFACE);
                                            }
                                            else {
                                                ClientInterface.switchScene(PageType.PROFILE_INTERFACE);
                                            }
                            }
                            case Check -> {}
                        }
                        break;

                    case Message_Post:
                        ClientInterface.fillProfileInterface(getLoggedUser());
                        break;

                    case Message_Get_Experts:
                        MessageGetExpertsByTag messageGetExpertsByTag = (MessageGetExpertsByTag) message;
                        ClientInterface.fillExpertsByTag(messageGetExpertsByTag.getObject());
                        break;

                    case Message_Get_Post_Data:
                        MessageGetPostData messageGetPostData = (MessageGetPostData) message;
                        ClientInterface.fillFullPostInterface(messageGetPostData.getObject());
                        break;

                    case Message_Get_Posts_By_Parameter:
                        MessageGetPostsByParameter messageGetPostsByParameter = (MessageGetPostsByParameter) message;
                        switch (messageGetPostsByParameter.getParameter()){
                            case Username -> ClientInterface.fillUserPostInterface(messageGetPostsByParameter.getObject(), messageGetPostsByParameter.getValue()); // utilizzabile per il fill sia del personal profile che di quello esterno
                            case Id   -> ClientInterface.fillFullPostInterface(messageGetPostsByParameter.getObject().get(0));
                            case Text -> ClientInterface.fillPostSearchInterface(messageGetPostsByParameter.getObject());
                        }
                        break;

                    case Message_Get_User_Data:
                        MessageGetUserData messageGetUserData = (MessageGetUserData) message;
                        if (messageGetUserData.getProfileType())
                            ClientInterface.fillProfileInterface(messageGetUserData.getObject());
                        else
                            ClientInterface.loadExternalProfile(messageGetUserData.getObject(), messageGetUserData.getPageType());
                        break;

                    case Message_Get_User_Answers:
                        MessageGetAnswers messageGetAnswers = (MessageGetAnswers) message;
                        ClientInterface.fillAnswersUsers(messageGetAnswers.getObject());
                        break;

                    case Message_Follow:
                        MessageFollow messageFollow = (MessageFollow) message;
                        switch (messageFollow.getOperation()){
                            case Create -> ClientInterface.setUnfollowUser();
                            case Delete -> ClientInterface.setFollowUser();
                            case Check -> ClientInterface.setFollowUnfollowUser(messageFollow.getUser());
                        }

                        break;

                    case Message_Get_Top_Users_Posts:
                        MessageGetTopUsersPosts messageGetTopUsersPosts = (MessageGetTopUsersPosts) message;
                        ClientInterface.fillTopUsersPosts(messageGetTopUsersPosts.getObject());
                        break;

                    case Message_Analytics_Most_Popular_Tags:
                        MessageAnalyticMPTags messageAnalyticMPTags = (MessageAnalyticMPTags) message;
                        ClientInterface.fillMPTagChart(messageAnalyticMPTags.getObject());
                        break;

                    case Message_Analytics_User_Rank:
                        MessageAnalyticUserRanking messageAnalyticUserRanking = (MessageAnalyticUserRanking) message;
                        ClientInterface.fillUserRanking(messageAnalyticUserRanking.getObject());
                        break;

                    case Message_Analytic_Hot_Topics:
                        MessageAnalyticHotTopics messageAnalyticHotTopics = (MessageAnalyticHotTopics) message;
                        ClientInterface.fillHotTopicsUsers(messageAnalyticHotTopics.getObject());
                        break;

                    case Message_Get_Correlated_Users:
                        MessageGetCorrelatedUsers messageGetCorrelatedUsers = (MessageGetCorrelatedUsers) message;
                        ClientInterface.fillCorrelatedUsersList(messageGetCorrelatedUsers.getObject());
                        break;

                    case Message_Get_Recommended_Users:
                        MessageGetRecommendedUsers messageGetRecommendedUsers = (MessageGetRecommendedUsers) message;
                        ClientInterface.fillPersonalRecommendedUsers(messageGetRecommendedUsers.getObject());
                        break;

                    case Message_Get_Follow_Data:
                        MessageGetFollowData messageGetFollowData = (MessageGetFollowData) message;
                        if (messageGetFollowData.getType()){
                            ClientInterface.fillFollowerList(messageGetFollowData.getObject());
                        }
                        else {
                            ClientInterface.fillFollowedList(messageGetFollowData.getObject());
                        }

                        break;
                }

            }

        }catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection closed, message format not valid");
            e.printStackTrace();
        }
    }

    public Message receive() throws IOException, ClassNotFoundException {
        return (Message) messageInputStream.readObject();
    }

    public void send(Message messaggio) throws IOException {
        messageOutputStream.reset();
        messageOutputStream.writeObject(messaggio);
    }

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public User getLoggedUser() { return loggedUser; }
}
