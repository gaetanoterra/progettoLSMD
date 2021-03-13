package server;

//import client.clientInterface;
import middleware.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

//classe preposta a ricevere le richieste dal client e richiamare le funzioni del DBManager
public class ClientManager extends Thread{

    private final DBManager dbManager;
    private final Socket socketUser;
    private User loggedUser;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private static final int DEFAULT_NUM_EXPERTS = 10;

    public ClientManager(Socket socketUser, DBManager dbManager) throws IOException{
        this.socketUser = socketUser;
        this.dbManager = dbManager;
        inputStream = new ObjectInputStream(socketUser.getInputStream());
        outputStream = new ObjectOutputStream(socketUser.getOutputStream());
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
                        //TODO: Controllare se la password conservata nel DB è un hash oppure no
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
                        MessageGetExpertsByTag msgExperts = (MessageGetExpertsByTag)msg;
                        User[] expertUsers = dbManager.findTopExpertsByTag(
                                msgExperts.getTag(),
                                DEFAULT_NUM_EXPERTS
                        );

                        send(new MessageGetExpertsByTag(msgExperts.getTag(), (ArrayList<User>)Arrays.asList(expertUsers)));
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
                                dbManager.removePost(post);
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
                                dbManager.insertRelationVote(loggedUser.getUserId(), answer.getAnswerId(), msgVote.getVoto());
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

                        Post[] resultPost;
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

                            default:
                                resultPost = null;
                        }
                        send(new MessageGetPostByParameter(null, null, resultPost));
                        break;

                    case Message_Get_User_Data:
                        MessageGetUserData msgGetUserData = (MessageGetUserData)msg;
                        User userToSearch = msgGetUserData.getObject().get(0);
                        String displayName = userToSearch.getDisplayName();
                        User userWithCompleteData = dbManager.getUserData(displayName);
                        send(
                                new MessageGetUserData(
                                        new ArrayList<>(Arrays.asList(userWithCompleteData))
                                )
                        );
                        break;

                    case Message_Get_Post_Data:
                        MessageGetPostData msgGetPostData = (MessageGetPostData)msg;
                        Post postToSearch = msgGetPostData.getObject().get(0);
                        String postId = postToSearch.getPostId();
                        Post postWithCompleteData = dbManager.getPostById(postId);
                        send(
                                new MessageGetPostData(
                                        new ArrayList<>(Arrays.asList(postWithCompleteData))
                                )
                        );
                        break;

                    case Message_Get_Top_Users_Posts:
                        HashMap<User, Post[]> mapUsersPosts = (HashMap<User, Post[]>)dbManager.findMostAnsweredTopUserPosts();
                        send(
                                new MessageGetTopUsersPosts(
                                        mapUsersPosts
                                )
                        );
                        break;

                    case Message_Update_User_data:
                        MessageUser messageUser = (MessageUser)msg;
                        User updatedUser = messageUser.getUser();
                        dbManager.updateUserData(updatedUser);
                        //TODO: Questo metodo si rifà al package client, qua non dovrebbe starci. Commento per sicurezza
                        // Main.setLog(updatedUser);
                        break;
                }
            }

        }
        catch (IOException | OpcodeNotValidException | ClassNotFoundException ioe) {ioe.printStackTrace();}
        finally {
            try {
                if (!this.socketUser.isClosed()) {
                    this.socketUser.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Message receive() throws IOException, ClassNotFoundException {
        return (Message) inputStream.readObject();
    }

    public void send(Message message) throws IOException {
        outputStream.writeObject(message);
    }

}
