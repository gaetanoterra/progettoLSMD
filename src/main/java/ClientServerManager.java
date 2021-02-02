import java.io.*;
import java.net.*;

public class ClientServerManager extends Thread {

    private ObjectOutputStream oos;
    private  ObjectInputStream ois;
    private Socket socket;
    private boolean in_attesa = true; //vedo se sto aspettando la risposta del server
    private boolean connesso_al_server = false;
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
            send(new MessageGetPostData(Opcode.Message_Get_Post_Data));
            System.out.println("richiesta connessione inviata al server");

            while(true){
                Message msg = (Message)ois.readObject();
                System.out.println("ricevuto messaggio dal server di tipo:" + msg.getOpcode());

                switch (msg.getOpcode()){
                    case Message_Ok:
                        break;

                    case Message_Fail:
                        break;

                    case Message_Login:
                        break;

                    case Message_Logout:
                        break;

                    case Message_Signup:
                        break;

                    case Message_Get_Experts:
                        break;

                    case Message_Create_Delete:
                        break;

                    case Message_Get_Post_Data:
                        break;

                    case Message_Get_Top_Users_Post:
                        break;
                }

            }

        } catch (EOFException e) {
            System.out.println("Il server ha rifutato la connessione");
            synchronized(this){
                connesso_al_server = false;
                in_attesa=false;
                notifyAll();
            }
        }catch (IOException | ClassNotFoundException e) {
            System.out.println("connessione chiusa");
            return;
        }
    }

    public Message receive() throws IOException, ClassNotFoundException {
        return (Message)ois.readObject();
    }

    public void send(Message messaggio) throws IOException {
        oos.writeObject(messaggio);
    }
}
