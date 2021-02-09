package client;

import middleware.*;

import java.io.*;
import java.net.*;

//classe preposta a ricevere le risposte dal server e passare i risultati ai vari Controller
public class ClientServerManager extends Thread {

    private ObjectOutputStream oos;
    private  ObjectInputStream ois;
    private Socket socket;
    private boolean in_attesa = true; //vedo se sto aspettando la risposta del server
    private boolean last_server_answer = false;
    private  int portNumber;

    public ClientServerManager() {
    }
    public ClientServerManager(int porta, InetAddress in, String n) throws IOException{
        portNumber = porta;
        socket = new Socket(in, porta);
        oos = new  ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
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
                            ControllerProfileInterface.fillProfileInterface(msgl.getUser());
                            ControllerAnonymousInterface.setLoggedInterface(msgl.getUser().getDisplayName());
                            Main.setLog(msgl.getUser());
                            last_server_answer = true;
                        }
                        in_attesa = false;
                        break;

                    case Message_Logout:
                        Main.resetLog();
                        ControllerAnonymousInterface.resetInterface();
                        break;

                    case Message_Signup:
                        MessageSignUp msgs = (MessageSignUp)msg;

                        if(msgs.getStatus() == StatusCode.Message_Ok)
                            last_server_answer = true;

                        in_attesa = false;
                        break;

                    case Message_Get_Experts:
                        break;

                    case Message_Get_Post_Data:
                        break;

                    case Message_Get_Post:
                        //chiamo una funzione di ControllerAnonymousInterface per popolare il pane con i Post
                        ControllerAnonymousInterface.setPosts(((MessageGetPostByParameter)msg).getPost());
                        break;

                    case Message_Get_Top_Users_Posts:
                        break;
                }

            }

        } catch (EOFException e) {
            System.out.println("Il server ha rifutato la connessione");
            synchronized(this){
                in_attesa=false;
                notifyAll();
            }
        }catch (IOException | ClassNotFoundException e) {
            System.out.println("connessione chiusa");
        }
    }

    public Message receive() throws IOException, ClassNotFoundException {
        return (Message)ois.readObject();
    }

    public void send(Message messaggio) throws IOException {
        in_attesa = true;
        oos.writeObject(messaggio);
    }

    public boolean checkLastServerAnswer() throws InterruptedException {
        while(in_attesa)
            wait();

        return last_server_answer;
    }
}
