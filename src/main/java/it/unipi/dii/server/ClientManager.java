package it.unipi.dii.server;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Messages.*;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import it.unipi.dii.server.databaseDriver.DBManager;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

//classe preposta a ricevere le richieste dal client e richiamare le funzioni del DBManager
//prova branch-gaetano
public class ClientManager extends Thread{

    private final DBManager dbManager;
    private final Socket socketUser;
    private User loggedUser;
    private final ObjectOutputStream clientOutputStream;
    private final ObjectInputStream clientInputStream;
    private static final int DEFAULT_NUM_EXPERTS = 10;

    public ClientManager(Socket socketUser, DBManager dbManager) throws IOException{
        this.socketUser = socketUser;
        this.dbManager  = dbManager;
        clientInputStream  = new ObjectInputStream(socketUser.getInputStream());
        clientOutputStream = new ObjectOutputStream(socketUser.getOutputStream());
    }

    public void run(){
        try{
            while(true) {
                Message msg = receive();
                System.out.println("received " + msg);

                switch (msg.getOpcode()){

                    case Message_Login:
                        MessageLogin msgl = (MessageLogin)msg;
                        String userDisplayName = msgl.getUser().getDisplayName();
                        String password = msgl.getUser().getPassword();

                        //chiedo al db i dati dell'utente corrispondente allo username
                        User user = dbManager.getUserDataByUsername(userDisplayName);
                        //controllo se la password dello username trovato corrisponde a quella passata dal client
                        if(user.getPassword() != null && user.getPassword().equals(password)){
                            loggedUser = user;
                            user.setLastAccessDate(Instant.now().toEpochMilli());
                            send(new MessageLogin(user, StatusCode.Message_Ok));
                            dbManager.updateUserData(user);
                            if (user.isAdmin()) {
                                System.out.println("Admin " + user.getDisplayName() + " logged in");
                            }
                        }
                        else
                            send(new MessageLogin(null, StatusCode.Message_Fail));
                        break;

                    case Message_Logout:
                        send(new MessageLogOut(loggedUser.getDisplayName()));
                        loggedUser = null;
                        break;

                    case Message_Signup:
                        MessageSignUp messageSignUp = (MessageSignUp)msg;
                        User signupUser = messageSignUp.getUser();
                        if(dbManager.insertUser(signupUser)){
                            loggedUser = dbManager.getUserDataByUsername(signupUser.getDisplayName());
                            send(new MessageSignUp(dbManager.getUserDataByUsername(signupUser.getDisplayName()), StatusCode.Message_Ok));
                        }
                        else{
                            send(new MessageSignUp(StatusCode.Message_Fail));
                        }
                        break;

                    case Message_Get_Experts:
                        if (loggedUser == null) {
                            break;
                        }
                        MessageGetExpertsByTag msgExperts = (MessageGetExpertsByTag)msg;
                        String[] expertUsers = dbManager.findTopExpertsByTag(
                                msgExperts.getTag(),
                                DEFAULT_NUM_EXPERTS
                        );

                        send(new MessageGetExpertsByTag(msgExperts.getTag(), expertUsers));
                        break;

                    case Message_Post:
                        if (loggedUser == null) {
                            break;
                        }
                        MessagePost msgPost = (MessagePost)msg;
                        Post post = msgPost.getPost();

                        switch (msgPost.getOperation()) {
                            case Create -> {
                                post.setOwnerUserId(loggedUser.getUserId());
                                post.setOwnerUserName(loggedUser.getDisplayName());
                                post.setViews(0L);
                                dbManager.insertPost(post);
                            }
                            case Delete -> {
                                if (loggedUser.isAdmin()) {
                                    System.out.println("Admin " + loggedUser.getDisplayName() + " removing post " + post.getPostId() + ".");
                                    dbManager.removePost(post);
                                }
                                else {
                                    System.out.println("User " + loggedUser.getDisplayName() + " is not admin. Check IsAdmin property.");
                                }
                            }
                            default -> throw new OpcodeNotValidException("Received Message_Post with unknown opcode");
                        }
                        break;

                    case Message_Answer:
                        if (loggedUser == null) {
                            break;
                        }
                        MessageAnswer msgAnswer = (MessageAnswer)msg;
                        Answer answer = msgAnswer.getAnswer();

                        switch (msgAnswer.getOperation()) {
                            case Create -> {
                                // id della risposta e' settato in dbManager
                                answer.setCreationDate(Instant.now().toEpochMilli());
                                answer.setScore(0);
                                answer.setOwnerUserName(loggedUser.getDisplayName());
                                answer.setOwnerUserId(loggedUser.getUserId());
                                dbManager.insertAnswer(answer, msgAnswer.getPostId());
                            }
                            case Delete -> {
                                if (loggedUser.isAdmin()) {
                                    System.out.println("Admin " + loggedUser.getDisplayName() + " removing answer " + answer.getAnswerId() + ".");
                                    dbManager.removeAnswer(answer, msgAnswer.getPostId());
                                }
                                else {
                                    System.out.println("User " + loggedUser.getDisplayName() + " is not admin. Check IsAdmin property.");
                                }
                            }
                            default -> throw new OpcodeNotValidException("Opcode of Message_Answer " +
                                                                            msgAnswer.getOperation() + " not valid");
                        }
                        break;

                    case Message_User:
                        if (loggedUser == null) {
                            break;
                        }
                        MessageUser msgUser = (MessageUser)msg;
                        user = msgUser.getUser();

                        switch (msgUser.getOperation()) {
                            case Create -> dbManager.insertUser(user);
                            case Delete -> {
                                if (loggedUser.isAdmin()) {
                                    System.out.println("Admin " + loggedUser.getDisplayName() + " removing user " + user.getUserId() + ".");
                                    dbManager.removeUser(user);
                                }
                                else {
                                    System.out.println("User " + loggedUser.getDisplayName() + " is not admin. Check IsAdmin property.");
                                }
                            }
                            default -> throw new OpcodeNotValidException("Opcode of Message_User " +
                                                                                msgUser.getOperation() + " not valid");
                        }
                        break;

                    case Message_Follow:
                        if (loggedUser == null) {
                            break;
                        }
                        MessageFollow msgFollow = (MessageFollow)msg;
                        switch (msgFollow.getOperation()) {
                            case Create -> dbManager.insertFollowRelationAndUpdate(loggedUser.getUserId(),
                                                                                        msgFollow.getUser().getUserId());
                            case Delete -> dbManager.removeFollowRelationAndUpdate(loggedUser.getUserId(),
                                                                                        msgFollow.getUser().getUserId());
                            default -> throw new OpcodeNotValidException("Opcode of Message_Follow" +
                                                                                msgFollow.getOperation() + " not valid");
                        }
                        break;

                    case Message_Vote:
                        if (loggedUser == null) {
                            break;
                        }
                        MessageVote msgVote = (MessageVote)msg;
                        answer = msgVote.getAnswer();
                        if (loggedUser.getDisplayName().equals(answer.getOwnerUserName())) {
                            // can't vote yourself
                            break;
                        }
                        switch (msgVote.getOperation()) {
                            case Create -> dbManager.insertRelationVote(
                                    loggedUser.getUserId(),
                                    answer.getAnswerId(),
                                    answer.getPostId(),
                                    msgVote.getVoto()
                            );
                            case Delete -> dbManager.removeRelationVote(
                                    loggedUser.getUserId(),
                                    answer.getAnswerId(),
                                    answer.getPostId(),
                                    msgVote.getVoto()
                            );
                            default     -> throw new OpcodeNotValidException("Opcode of Message_Vote" +
                                                                                msgVote.getOperation() + " not valid");
                        }
                        break;

                    case Message_Get_Posts_By_Parameter:
                        MessageGetPostByParameter msgParameter = (MessageGetPostByParameter) msg;

                        ArrayList<Post>  postArrayList = new ArrayList<>();

                        switch (msgParameter.getParameter()) {
                            case Date -> postArrayList = dbManager.getPostByDate(msgParameter.getValue());
                            case Tags -> {
                                String[] tags = msgParameter.getValue().split(";");
                                postArrayList.addAll(dbManager.getPostsByTag(tags));
                            }
                            case Text -> postArrayList.addAll(dbManager.getPostsByText(msgParameter.getValue()));
                            case Username -> postArrayList = dbManager.getPostByOwnerUsername(msgParameter.getValue());
                            case Id -> postArrayList.add(dbManager.getPostById(msgParameter.getValue()));
                        }
                        System.out.println("sending out new Message_Get_Posts_By_Parameter containing " +
                                            postArrayList.size() +
                                            " posts"
                        );
                        send(new MessageGetPostByParameter(msgParameter.getParameter(), null,  postArrayList));
                        break;

                    case Message_Get_User_Data:
                        MessageGetUserData msgGetUserData = (MessageGetUserData)msg;
                        User userToSearch = msgGetUserData.getObject();
                        if (userToSearch.getDisplayName() != null) {
                            String displayName = userToSearch.getDisplayName();
                            User userWithCompleteData = dbManager.getUserDataByUsername(displayName);
                            send(new MessageGetUserData(userWithCompleteData, msgGetUserData.getProfileType(), msgGetUserData.getPageType()));
                        }
                        else if (userToSearch.getUserId() != null) {
                            String userId = userToSearch.getUserId();
                            User userWithCompleteData = dbManager.getUserDataById(userId);
                            send(new MessageGetUserData(userWithCompleteData, msgGetUserData.getProfileType(), msgGetUserData.getPageType()));
                        }


                        break;

                    case Message_Get_Post_Data:
                        //TODO: Non usato

                        break;

                    case Message_Get_Top_Users_Posts:
                        if (loggedUser == null) {
                            break;
                        }
                        send(new MessageGetTopUsersPosts(
                                dbManager.findMostAnsweredTopUserPosts()));
                        break;

                    case Message_Update_User_data:
                        if (loggedUser == null) {
                            break;
                        }
                        MessageUser messageUser = (MessageUser)msg;
                        User updatedUser = messageUser.getUser();
                        dbManager.updateUserData(updatedUser);
                        break;

                    case Message_Analytics_Most_Popular_Tags:
                        if (loggedUser == null) {
                            break;
                        }
                        MessageAnalyticMPTags messageAnalyticMPTags = (MessageAnalyticMPTags) msg;
                        messageAnalyticMPTags.setTags(dbManager.findMostPopularTags());
                        send(messageAnalyticMPTags);
                        break;

                    case Message_Analytics_Most_Popular_Tags_Location:
                        if (loggedUser == null) {
                            break;
                        }
                        MessageAnalyticMPTagsLocation messageAnalyticMPTagsLocation = (MessageAnalyticMPTagsLocation) msg;
                        messageAnalyticMPTagsLocation.setTags(dbManager.findMostPopularTagsByLocation(messageAnalyticMPTagsLocation.getLocation(), messageAnalyticMPTagsLocation.getNumTags()));
                        send(messageAnalyticMPTagsLocation);
                        break;

                    case Message_Analytics_User_Rank:
                        if (loggedUser == null) {
                            break;
                        }
                        MessageAnalyticUserRanking messageAnalyticUserRanking = (MessageAnalyticUserRanking) msg;
                        messageAnalyticUserRanking.setUsers(dbManager.getUsersRank());
                        send(messageAnalyticUserRanking);
                        break;

                    case Message_Analytic_Hot_Topics:
                        if (loggedUser == null) {
                            break;
                        }
                        MessageAnalyticHotTopics messageAnalyticHotTopics = (MessageAnalyticHotTopics) msg;
                        messageAnalyticHotTopics.setMap(dbManager.findHotTopicsforTopUsers());
                        send(messageAnalyticHotTopics);
                        break;


                }
            }

        }catch( SocketException | EOFException eof) {
            System.out.println(((this.loggedUser != null) ? this.loggedUser.getDisplayName() : "Anonymous user") + " just closed the connection");
        } catch (IOException | OpcodeNotValidException | ClassNotFoundException ioe) {ioe.printStackTrace();}

        try {
            this.clientInputStream.close();
            this.clientOutputStream.close();
            this.socketUser.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public Message receive() throws IOException, ClassNotFoundException {
        return (Message) clientInputStream.readObject();
    }

    public void send(Message message) throws IOException {
        clientOutputStream.writeObject(message);
    }

}
