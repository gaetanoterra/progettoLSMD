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
    private boolean last_server_answer = false;
    private  int portNumber;
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
                        if(msgl.getStatus() == StatusCode.Message_Ok) {
                            last_server_answer = true;
                        }
                        ClientInterface.loginResponseHandler(msgl);
                        break;

                    case Message_Logout:
                        ClientInterface.resetLog();
                        // ClientInterface.getControllerAnonymousInterface().resetInterface();
                        return;

                    case Message_Signup:
                        MessageSignUp messageSignUp = (MessageSignUp)message;
                        ClientInterface.registrationResponseHandler(messageSignUp);
                        break;

                    case Message_Get_Experts:

                        break;

                    case Message_Get_Post_Data:
                        MessageGetPostData messageGetPostData = (MessageGetPostData) message;
                        ClientInterface.fillFullPostInterface(messageGetPostData.getObject());
                        break;

                    case Message_Get_Posts_By_Parameter:
                        MessageGetPostByParameter messageGetPostByParameter = (MessageGetPostByParameter) message;
                        switch (messageGetPostByParameter.getParameter()){
                            case Username -> {}
                            case Id   -> ClientInterface.fillFullPostInterface(messageGetPostByParameter.getPostArrayList().get(0));
                            case Text -> ClientInterface.fillPostSearchInterface(messageGetPostByParameter.getPostArrayList());
                        }
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

}
