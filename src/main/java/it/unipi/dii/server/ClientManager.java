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

//classe preposta a ricevere le richieste dal client e richiamare le funzioni del DBManager
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
                System.out.println("Received message with opCode: " + msg.getOpcode());

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
                                post.setViews(0);
                                dbManager.insertPost(post);
                            }
                            case Delete -> {
                                if (loggedUser.isAdmin() ||
                                        loggedUser.getUserId().equals(post.getOwnerUserId()) ||
                                        loggedUser.getDisplayName().equals(post.getOwnerUserName())
                                ) {
                                    System.out.println((loggedUser.isAdmin() ? "Admin " : "Owner ") + loggedUser.getDisplayName() + " removing post " + post.getMongoPost_id() + ".");
                                    dbManager.removePost(post);
                                }
                                else {
                                    System.out.println("User " + loggedUser.getDisplayName() + " is not admin nor owner of post " + post.getMongoPost_id() + ".");
                                }
                            }
                            default -> throw new OpcodeNotValidException("Received Message_Post with unknown opcode");
                        }

                        send(msgPost);
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
                                dbManager.insertAnswer(answer);
                            }
                            case Delete -> {
                                if (loggedUser.isAdmin() ||
                                        loggedUser.getUserId().equals(answer.getOwnerUserId()) ||
                                        loggedUser.getDisplayName().equals(answer.getOwnerUserName())
                                ) {
                                    System.out.println((loggedUser.isAdmin() ? "Admin " : "Owner ") + loggedUser.getDisplayName() + " removing answer " + answer.getAnswerId() + ".");
                                    dbManager.removeAnswer(answer);
                                }
                                else {
                                    System.out.println("User " + loggedUser.getDisplayName() + " Message_Answer of answer " + answer.getAnswerId() + ".");
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
                                dbManager.removeUser(user);
                                System.out.println("Logged user: " + loggedUser.getDisplayName());
                                System.out.println("User inside message: " + user.getDisplayName());
                                if (user.getDisplayName().equals(loggedUser.getDisplayName())){
                                    //se sto eliminando il mio account,effettuo un logout e torno alla postsearchinterface
                                    loggedUser = null;
                                    msgUser.setUser(null);
                                    send(msgUser);
                                }
                                else{
                                    //se è l'admin che sta eliminando il profilo di un altro,dopo l'eliminazione voglio tornare al profilo admin
                                    send(msgUser);
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
                            case Create -> dbManager.insertFollowRelationAndUpdate(loggedUser.getDisplayName(), msgFollow.getUser().getDisplayName());
                            case Delete -> dbManager.removeFollowRelationAndUpdate(loggedUser.getDisplayName(), msgFollow.getUser().getDisplayName());
                            case Check -> {
                                boolean result = dbManager.checkFollowRelation(loggedUser.getDisplayName(), msgFollow.getUser().getDisplayName());
                                if (!result) {
                                    msgFollow.setUser(null);
                                }
                            }
                            default -> throw new OpcodeNotValidException("Opcode of Message_Follow" +
                                                                                msgFollow.getOperation() + " not valid");
                        }

                        send(msgFollow);
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
                                    loggedUser.getDisplayName(),
                                    answer.getAnswerId(),
                                    answer.getParentPostId(),
                                    msgVote.getVoto()
                            );
                            case Delete -> dbManager.removeRelationVote(
                                    loggedUser.getDisplayName(),
                                    answer.getAnswerId(),
                                    answer.getParentPostId(),
                                    msgVote.getVoto()
                            );
                            default     -> throw new OpcodeNotValidException("Opcode of Message_Vote" +
                                                                                msgVote.getOperation() + " not valid");
                        }
                        break;

                    case Message_Get_Posts_By_Parameter:
                        MessageGetPostsByParameter msgParameter = (MessageGetPostsByParameter) msg;

                        ArrayList<Post>  postArrayList = new ArrayList<>();

                        switch (msgParameter.getParameter()) {
                            case Text -> postArrayList.addAll(dbManager.getPostsByText(msgParameter.getValue()));
                            case Username -> postArrayList = dbManager.getPostsByOwnerUsername(msgParameter.getValue());
                            case Id -> postArrayList.add(dbManager.getPostById(msgParameter.getValue()));
                        }
                        System.out.println("Sending out a new Message_Get_Posts_By_Parameter containing " +
                                            postArrayList.size() +
                                            " posts"
                        );
                        send(new MessageGetPostsByParameter(msgParameter.getParameter(), ((MessageGetPostsByParameter) msg).getValue(),  postArrayList));
                        break;

                    case Message_Get_User_Data:
                        MessageGetUserData msgGetUserData = (MessageGetUserData)msg;
                        User userToSearch = msgGetUserData.getObject();

                        if (userToSearch.getDisplayName() != null) {
                            String displayName = userToSearch.getDisplayName();
                            User userWithCompleteData = dbManager.getUserDataByUsername(displayName);
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

                    case Message_Get_Correlated_Users:
                        MessageGetCorrelatedUsers messageGetCorrelatedUsers = (MessageGetCorrelatedUsers) msg;
                        messageGetCorrelatedUsers.setUserList(dbManager.getCorrelatedUsers(messageGetCorrelatedUsers.getUser()));
                        send(messageGetCorrelatedUsers);
                        break;

                    case Message_Get_Recommended_Users:
                        MessageGetRecommendedUsers messageGetRecommendedUsers = (MessageGetRecommendedUsers) msg;
                        messageGetRecommendedUsers.setUsers(dbManager.getRecommendedUsers(messageGetRecommendedUsers.getDisplayName(), messageGetRecommendedUsers.getTag()));
                        send(messageGetRecommendedUsers);
                        break;

                    case Message_Get_User_Answers:
                        MessageGetAnswers messageGetAnswers = (MessageGetAnswers) msg;
                        messageGetAnswers.setAnswerArrayList(dbManager.getUserAnswer(messageGetAnswers.getDisplayName()));
                        send(messageGetAnswers);
                        break;

                    case Message_Get_Follow_Data:
                        MessageGetFollowData messageGetFollowData = (MessageGetFollowData) msg;
                        if (messageGetFollowData.getType()){
                            messageGetFollowData.setFollowers(dbManager.getUserIdsFollower(messageGetFollowData.getUserDisplayName()));
                        }
                        else{
                            messageGetFollowData.setFollowers(dbManager.getUserIdsFollowed(messageGetFollowData.getUserDisplayName()));
                        }

                        send(messageGetFollowData);
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
