package server;

import client.Main;
import middleware.*;

import java.io.*;
import java.net.*;

//classe preposta a ricevere le richieste dal client e richiamare le funzioni del DBManager
public class ClientManager extends Thread{

    private DBManager dbManager;
    private Socket socket;
    private User loggedUser;
    private ObjectOutputStream oos;
    private  ObjectInputStream ois;

    public ClientManager(Socket socket,DBManager dbm) throws IOException{
        this.socket = socket;
        this.dbManager = dbm;
        ois = new ObjectInputStream(socket.getInputStream());
        oos = new ObjectOutputStream(socket.getOutputStream());
    }

    public void run(){
        try{
            while(true) {
                Message msg = receive();

                switch (msg.getOpcode()){

                    case Message_Login:
                        MessageLogin msgl = (MessageLogin)msg;
                        String userDisplayName = msgl.getUser().getDisplayName();
                        String password = msgl.getUser().getPassword();

                        //chiedo al db i dati dell'utente corrispondente allo username
                        User user = dbManager.getUserData(userDisplayName);

                        //controllo se la password dello username trovato corrisponde a quella passata dal client
                        if(user.getPassword().equals(password)){
                            loggedUser = user;
                            //aggiorno la lastAcessDate dell'utente loggato a questo istante
                            //loggedUser.setLastAccessDate(new Date());
                            send(new MessageLogin(user, StatusCode.Message_Ok));
                        }
                        else
                            send(new MessageLogin(null, StatusCode.Message_Fail));
                        break;

                    case Message_Logout:
                        loggedUser = null;
                        return;

                    case Message_Signup:
                        MessageSignUp messageSignUp = (MessageSignUp)msg;

                        if(dbManager.insertUser(messageSignUp.getUser())){
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
                        Post post = msgPost.getPost();

                        switch (msgPost.getOperation()) {
                            case Create:
                                post.setOwnerUserId(loggedUser.getUserId());
                                dbManager.insertPost(post);
                                break;
                            case Delete:
                                dbManager.removePost(post, loggedUser.getUserId());
                                break;
                            default:
                                throw new OpcodeNotValidException("You are not supposed to be here");
                        }
                        break;

                    case Message_Answer:
                        MessageAnswer msgAnswer = (MessageAnswer)msg;
                        Answer answer = msgAnswer.getAnswer();

                        switch (msgAnswer.getOperation()) {
                            case Create:
                                answer.setOwnerUserId(loggedUser.getUserId());
                                dbManager.insertAnswer(answer, msgAnswer.getPostId());
                                break;
                            case Delete:
                                dbManager.removeAnswer(answer, msgAnswer.getPostId());
                                break;
                            default:
                                throw new OpcodeNotValidException("Opcode of Message_Answer " + msgAnswer.getOperation() + " not valid");
                        }
                        break;
                    case Message_User:
                        MessageUser msgUser = (MessageUser)msg;
                        user = msgUser.getUser();

                        switch (msgUser.getOperation()) {
                            case Create:
                                dbManager.insertUser(user);
                                break;
                            case Delete:
                                dbManager.removeUser(user);
                                break;
                            default:
                                throw new OpcodeNotValidException("Opcode of Message_User " + msgUser.getOperation() + " not valid");
                        }
                        break;
                    case Message_Follow:
                        MessageFollow msgFollow = (MessageFollow)msg;
                        switch (msgFollow.getOperation()) {
                            case Create:
                                dbManager.insertFollowRelationAndUpdate(loggedUser.getDisplayName(),msgFollow.getUser().getDisplayName());
                                break;
                            case Delete:
                                dbManager.removeFollowRelationAndUpdate(loggedUser.getDisplayName(), msgFollow.getUser().getDisplayName());
                                break;
                            default:
                                throw new OpcodeNotValidException("Opcode of Message_Follow" + msgFollow.getOperation() + " not valid");
                        }
                        break;
                    case Message_Vote:
                        MessageVote msgVote = (MessageVote)msg;
                        answer = msgVote.getAnswer();

                        switch (msgVote.getOperation()) {
                            case Create:
                                dbManager.insertRelationVote(answer.getAnswerId(), loggedUser.getUserId(), msgVote.getVoto());
                                break;
                            case Delete:
                                dbManager.removeRelationVote(loggedUser.getUserId(),answer.getAnswerId());
                                break;
                            default:
                                throw new OpcodeNotValidException("Opcode of Message_Vote" + msgVote.getOperation() + " not valid");
                        }
                        break;

                    case Message_Get_Post:
                        MessageGetPostByParameter msgParameter = (MessageGetPostByParameter) msg;

                        Post[] resultPost = new Post[0];
                        switch (msgParameter.getParameter()){
                            case Date:
                                resultPost = dbManager.getPostByDate(msgParameter.getValue());
                                break;

                            case Tags:
                                String[] tags = msgParameter.getValue().split(";");
                                resultPost = dbManager.getPostsByTag(tags);
                                break;

                            case Text:
                                resultPost = dbManager.getPostByText(msgParameter.getValue());
                                break;

                            case Username:
                                resultPost = dbManager.getPostByOwnerUsername(msgParameter.getValue());
                                break;

                        }
                        send(new MessageGetPostByParameter(null, null, resultPost));
                        break;

                    case Message_Get_Top_Users_Posts:
                        break;

                    case Message_Update_User_data:
                        dbManager.updateUserData(((MessageUser)msg).getUser());
                        Main.setLog(((MessageUser)msg).getUser());
                        break;
                }
            }

        }
        catch (IOException | OpcodeNotValidException | ClassNotFoundException ioe) {ioe.printStackTrace();}
    }

    public Message receive() throws IOException, ClassNotFoundException {
        return (Message)ois.readObject();
    }

    public void send(Message message) throws IOException {
        oos.writeObject(message);
    }

}
