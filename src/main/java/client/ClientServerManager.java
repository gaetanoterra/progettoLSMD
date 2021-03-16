package client;

import Libraries.Messages.*;

import java.io.*;
import java.net.*;

//classe preposta a ricevere le risposte dal server e passare i risultati ai vari Controller
public class ClientServerManager extends Thread {

    private ObjectOutputStream messageOutputStream;
    private  ObjectInputStream messageInputStream;
    private Socket clientSocket;
    private static boolean waiting = true; //vedo se sto aspettando la risposta del server
    private boolean last_server_answer = false;
    private  int portNumber;

    public ClientServerManager(int porta, InetAddress in) throws IOException{
        portNumber = porta;
        clientSocket = new Socket(in, porta);
        messageOutputStream = new  ObjectOutputStream(clientSocket.getOutputStream());
        messageInputStream  = new  ObjectInputStream(clientSocket.getInputStream());
    }

    public ClientServerManager(int porta, InetAddress in, ClientInterface cl) throws IOException{
        portNumber = porta;
        clientSocket = new Socket(in, porta);
        messageOutputStream = new  ObjectOutputStream(clientSocket.getOutputStream());
        messageInputStream = new ObjectInputStream(clientSocket.getInputStream());
    }
    public void run(){

        try {
            //richiedo al client di mostrarmi i post più recenti da visualizzare sulla schermata iniziale
            send(new MessageGetPostData());
            System.out.println("richiesta connessione inviata al server");

            while(true){
                Message msg = receive();
                System.out.println("ricevuto messaggio dal server di tipo:" + msg.getOpcode());

                switch (msg.getOpcode()){

                    case Message_Login:
                        MessageLogin msgl = (MessageLogin) msg;

                        //se ricevo l'utente devo chiamare una funzione che inserisca i dati dell'utente nell'interfaccia
                        if(msgl.getStatus() == StatusCode.Message_Ok) {
                            // ClientInterface.getControllerProfileInterface().fillProfileInterface(msgl.getUser());
                            //ClientInterface.getControllerAnonymousInterface().setLoggedInterface(msgl.getUser().getDisplayName());
                            // ClientInterface.setLog(msgl.getUser());
                            last_server_answer = true;
                        }
                        break;

                    case Message_Logout:
                        ClientInterface.resetLog();
                        // ClientInterface.getControllerAnonymousInterface().resetInterface();
                        break;

                    case Message_Signup:
                        MessageSignUp msgs = (MessageSignUp)msg;

                        if(msgs.getStatus() == StatusCode.Message_Ok)
                            last_server_answer = true;

                        break;

                    case Message_Get_Experts:

                        break;

                    case Message_Get_Post_Data:

                        break;

                    case Message_Get_Post:
                        //chiamo una funzione dell'istanza di ControllerAnonymousInterface per popolare il pane con i Post
                        // ClientInterface.getControllerAnonymousInterface().setPosts(((MessageGetPostByParameter)msg).getPost());
                        break;

                    case Message_Get_Top_Users_Posts:
                        break;
                }

            }

        }catch (IOException | ClassNotFoundException e) {
            System.out.println("Connection closed, message format not valid");
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
        ClientServerManager.waiting = in_attesa;
    }

    public boolean checkLastServerAnswer() throws InterruptedException {
        while(waiting)
            wait();

        return last_server_answer;
    }
}
