package server;

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
                Message msg = receive();

                switch (msg.getOpcode()){

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
                            send(new MessageLogin(user));
                        }
                        else
                            send(new MessageLogin(null));
                        break;

                    case Message_Logout:
                        loggedUser = null;
                        break;

                    case Message_Signup:
                        MessageSignUp msgs = (MessageSignUp)msg;

                        if(dbManager.insertUser(msgs.getUser())){
                            send(new MessageSignUp(StatusCode.Message_Ok));
                        }
                        else{
                            send(new MessageSignUp(StatusCode.Message_Fail));
                        }
                        break;

                    case Message_Get_Experts:
                        break;

                    case Message_Post:
                        MessagePost msgPost = (MessagePost)msg;
                        switch (msgPost.getOperation()) {
                            case Create:
                                break;
                            case Delete:
                                Post post = msgPost.getPost();
                                post.setOwnerUserId(loggedUser.getDisplayName());
                                dbManager.insertPost(post);
                                break;
                            default:
                                throw new Exception("You are not supposed to be here");
                        }
                        break;
                    case Message_Answer:
                        MessageAnswer msgAnswer = (MessageAnswer)msg;
                        switch (msgAnswer.getOperation()) {
                            case Create:
                                break;
                            case Delete:
                                break;
                            default:
                                throw new Exception("You are not supposed to be here");
                        }
                        break;
                    case Message_User:
                        MessageUser msgUser = (MessageUser)msg;
                        switch (msgUser.getOperation()) {
                            case Create:
                                break;
                            case Delete:
                                break;
                            default:
                                throw new Exception("You are not supposed to be here");
                        }
                        break;
                    case Message_Follow:
                        MessageFollow msgFollow = (MessageFollow)msg;
                        switch (msgFollow.getOperation()) {
                            case Create:
                                break;
                            case Delete:
                                break;
                            default:
                                throw new Exception("You are not supposed to be here");
                        }
                        break;
                    case Message_Vote:
                        MessageVote msgVote = (MessageVote)msg;
                        switch (msgVote.getOperation()) {
                            case Create:
                                break;
                            case Delete:
                                break;
                            default:
                                throw new Exception("You are not supposed to be here");
                        }
                        break;

                    case Message_Get_Post_Data:
                        break;

                    case Message_Get_Top_Users_Posts:
                        break;
                }
            }

        }
        catch (Exception e) {e.printStackTrace();}
    }

    public Message receive() throws IOException, ClassNotFoundException {
        return (Message)ois.readObject();
    }

    public void send(Message message) throws IOException {
        oos.writeObject(message);
    }

}
