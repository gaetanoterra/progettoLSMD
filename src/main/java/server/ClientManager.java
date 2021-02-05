package server;

import client.*;
import middleware.*;

import java.io.*;
import java.net.*;

public class ClientManager extends Thread{

    private DBManager dbManager;
    private Socket socket;
    private User loggedUser;
    private ObjectOutputStream oos;
    private  ObjectInputStream ois;

    public ClientManager(Socket socket) throws IOException{
        this.socket = socket;
        ois = new ObjectInputStream(socket.getInputStream());
        oos = new ObjectOutputStream(socket.getOutputStream());
        dbManager = new DBManager();
    }

    public void run(){
        try{
            while(true) {
                Message msg = (Message)ois.readObject();

                switch (msg.getOpcode()){
                    case Message_Ok:
                        break;

                    case Message_Fail:
                        break;

                    case Message_Login:
                        MessageLogin msgl = (MessageLogin)msg;
                        String u = msgl.getUser().getDisplayName();
                        String p = msgl.getUser().getPassword();

                        //chiedo al db i dati dell'utente corrispondente allo username
                        User user = dbManager.getUserData(u);

                        //controllo se la password dello username trovato corrisponde a quella passata dal client
                        if(user.getPassword().equals(p)){
                            loggedUser = user;
                            //aggiorno la lastAcessDate dell'utente loggato a questo istante
                            //loggedUser.setLastAccessDate(new Date());
                            send(new MessageLogin(Opcode.Message_Login, user));
                        }
                        else
                            send(new MessageLogin(Opcode.Message_Login, null));
                        break;

                    case Message_Logout:
                        loggedUser = null;
                        break;

                    case Message_Signup:
                        MessageSignUp msgs = (MessageSignUp)msg;

                        if(dbManager.insertUser(msgs.getUser())){
                            send(new MessageSignUp(Opcode.Message_Ok, null));
                        }
                        else{
                            send(new MessageSignUp(Opcode.Message_Fail, null));
                        }
                        break;

                    case Message_Get_Experts:
                        break;

                    case Message_Create_Post:
                        break;

                    case Message_Create_User:
                        break;

                    case Message_Create_Answer:
                        break;

                    case Message_Delete_Post:
                        MessageCreatePost msgc = (MessageCreatePost)msg;
                        Post post = msgc.getPost();

                        post.setOwnerUserId(loggedUser.getDisplayName());

                        dbManager.insertPost(post);

                        break;

                    case Message_Delete_User:
                        break;

                    case Message_Delete_Answer:
                        break;

                    case Message_Get_Post_Data:
                        break;

                    case Message_Get_Top_Users_Post:
                        break;
                }
            }

        }
        catch (Exception e) {e.printStackTrace();}
    }

    public Message receive() throws IOException, ClassNotFoundException {
        return (Message)ois.readObject();
    }

    public void send(Message messaggio) throws IOException {
        oos.writeObject(messaggio);
    }

}
