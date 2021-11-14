package it.unipi.dii.client;

import it.unipi.dii.Libraries.Messages.*;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.controllers.ControllerAnalysisInterface;
import it.unipi.dii.client.controllers.PageType;

import java.io.*;
import java.net.*;

//classe preposta a ricevere le risposte dal server e passare i risultati ai vari Controller
public class ServerConnectionManager extends Thread {

    private ObjectOutputStream messageOutputStream;
    private  ObjectInputStream messageInputStream;
    private Socket clientSocket;
    private static boolean waiting = true; //vedo se sto aspettando la risposta del server
    private int portNumber;
    private User loggedUser;

    public ServerConnectionManager(int porta, InetAddress in) throws IOException{
        portNumber = porta;
        clientSocket = new Socket(in, porta);
        messageOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        messageInputStream  = new ObjectInputStream(clientSocket.getInputStream());
    }

    public ServerConnectionManager(int porta, InetAddress in, ClientInterface cl) throws IOException{
        portNumber = porta;
        clientSocket = new Socket(in, porta);
        messageOutputStream = new  ObjectOutputStream(clientSocket.getOutputStream());
        messageInputStream = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void run(){

        try {
            System.out.println("richiesta connessione inviata al server");

            while(true){
                Message message = receive();
                System.out.println("ricevuto messaggio dal server di tipo:" + message.getOpcode());

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

                    case Message_Get_Experts:
                        MessageGetExpertsByTag messageGetExpertsByTag = (MessageGetExpertsByTag) message;
                        ClientInterface.fillExpertsByTag(messageGetExpertsByTag.getUsersList());
                        break;

                    case Message_Get_Post_Data:
                        MessageGetPostData messageGetPostData = (MessageGetPostData) message;
                        ClientInterface.fillFullPostInterface(messageGetPostData.getObject());
                        break;

                    case Message_Get_Posts_By_Parameter:
                        MessageGetPostByParameter messageGetPostByParameter = (MessageGetPostByParameter) message;
                        switch (messageGetPostByParameter.getParameter()){
                            case Username -> ClientInterface.fillUserPostInterface(messageGetPostByParameter.getPostArrayList(), messageGetPostByParameter.getValue()); // utilizzabile per il fill sia del personal profile che di quello esterno
                            case Id   -> ClientInterface.fillFullPostInterface(messageGetPostByParameter.getPostArrayList().get(0));
                            case Text -> ClientInterface.fillPostSearchInterface(messageGetPostByParameter.getPostArrayList());
                        }
                        break;

                    case Message_Get_User_Data:
                        MessageGetUserData messageGetUserData = (MessageGetUserData) message;
                        if(messageGetUserData.getProfileType() == true)
                            ClientInterface.loadExternalProfile(messageGetUserData.getObject().remove(0), messageGetUserData.getPageType()); //da modificare per il personal profile
                        else
                            ClientInterface.loadExternalProfile(messageGetUserData.getObject().remove(0), messageGetUserData.getPageType());
                        break;

                    case Message_Get_Top_Users_Posts:
                        break;

                    case Message_Analytics_Most_Popular_Tags:
                        MessageAnalyticMPTags messageAnalyticMPTags = (MessageAnalyticMPTags) message;
                        ClientInterface.fillMPTagChart(messageAnalyticMPTags.getTags());
                        break;

                    case Message_Analytics_Most_Popular_Tags_Location:
                        MessageAnalyticMPTagsLocation messageAnalyticMPTagsLocation = (MessageAnalyticMPTagsLocation) message;
                        ClientInterface.fillMPTagLocationChart(messageAnalyticMPTagsLocation.getTags());
                        break;

                    case Message_Analytics_User_Rank:
                        MessageAnalyticUserRanking messageAnalyticUserRanking = (MessageAnalyticUserRanking) message;
                        ClientInterface.fillUserRanking(messageAnalyticUserRanking.getUsers());
                        break;

                    case Message_Analytic_Hot_Topics:
                        MessageAnalyticHotTopics messageAnalyticHotTopics = (MessageAnalyticHotTopics) message;
                        ClientInterface.fillHotTopicsUsers(messageAnalyticHotTopics.getMap());
                        break;

                    case Message_Get_User_Followers:
                        MessageGetUserFollowers messageGetUserFollowers = (MessageGetUserFollowers) message;
                        ClientInterface.fillUserFollowerList(messageGetUserFollowers.getObject());
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
        waiting = true;
        messageOutputStream.writeObject(messaggio);
    }

    public static boolean isWaiting(){
        return waiting;
    }

    public static void setWaiting(boolean in_attesa) {
        ServerConnectionManager.waiting = in_attesa;
    }

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public User getLoggedUser() { return loggedUser; }
}
