package it.unipi.dii.server.databaseDriver;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;

import java.util.*;

//classe preposta a ricevere le richieste dal clientManager e a propagarle al documentDB e al graphDB
public class DBManager {

    private DocumentDBManager documentDBManager;
    private GraphDBManager graphDBManager;

    public DBManager(){
       this(DBExecutionMode.LOCAL);
    }

    public DBManager(DBExecutionMode dbe){
        documentDBManager = new DocumentDBManager(dbe);
        graphDBManager = new GraphDBManager();
    }
    public void close(){
        this.documentDBManager.close();
        this.graphDBManager.close();
    }

    /*
    --------------------------- ANALYTICS ---------------------------
     */
    public Map<User, Post[]> findMostAnsweredTopUserPosts(){
        return documentDBManager.findMostAnsweredTopUserPosts();
    }

    public User[] findTopExpertsByTag(String tagName, int numExperts){
        return documentDBManager.findTopExpertsByTag(tagName, numExperts);
    }

    //TODO: Non esiste un messaggio per ottenere i tag popolari tra gli utenti che stanno in una location. Creare un opcode e un messagio per questo
    public String[] findMostPopularTagsByLocation(String location, int numTags){
        return documentDBManager.findMostPopularTagsByLocation(location, numTags);
    }
    //TODO: Non esiste un messaggio per ottenere i tag più popolari in generale. Creare un opcode e un messagio per questo
    public Map<String, Integer> findMostPopularTags() {
        return graphDBManager.findMostPopularTags();
    }

    //TODO: Non esiste un messaggio per ottenere il ranking degli utenti. Creare un opcode e un messagio per questo
    public User[] getUsersRank(){
        return documentDBManager.getUsersRank();
    }

    //TODO: Non esiste un messaggio per ottenere gli utenti correlati. Creare un opcode e un messagio per questo
    //restituisco gli username degli utenti (nel graphDB ci sono solo quelli), poi quando seleziono uno specifico utente chiamo la getUserByUsername
    public String[] getCorrelatedUsers(String username){
        return graphDBManager.getCorrelatedUsers(username);
    }

    //TODO: Non esiste un messaggio per ottenere gli utenti raccomandati. Creare un opcode e un messagio per questo
    //restituisco gli username degli utenti (nel graphDB ci sono solo quelli), poi quando seleziono uno specifico utente chiamo la getUserByUsername
    public String[] getRecommendedUsers(String username, String tagName){
        return graphDBManager.getRecommendedUsers(username, tagName);
    }

    /*
    --------------------------- USERS ---------------------------
     */


    public User getUserData(String username){
        return documentDBManager.getUserData(username);
    }

    public boolean insertUser(User newUser){
        boolean insertedUser = documentDBManager.insertUser(newUser);
        if(insertedUser)
            graphDBManager.insertUser(newUser);
        return insertedUser;
    }

    public boolean removeUser(User user){
        //prima aggiorno gli attributi ridondanti follower e followed su mongodb
        ArrayList<String> userIdsFollower = graphDBManager.getUserIdsFollower(user.getUserId());
        ArrayList<String> userIdsFollowed = graphDBManager.getUserIdsFollowed(user.getUserId());
        for (String userIdFollower: userIdsFollower) {
            documentDBManager.removeUserFollowerAndFollowedRelation(userIdFollower, user.getUserId());
        }
        for (String userIdFollowed: userIdsFollowed) {
            documentDBManager.removeUserFollowerAndFollowedRelation(user.getUserId(), userIdFollowed);
        }
        //ora posso rimuovere l'utente
        boolean deletedUser = documentDBManager.removeUser(user.getUserId());
        if (deletedUser) {
            graphDBManager.removeUser(user.getUserId());
        }
        return deletedUser;
    }

    public boolean updateUserData(User user){
        return documentDBManager.updateUserData(user);
    }

    /*
    --------------------------- POSTS ---------------------------
     */

    public ArrayList<Post> getPostByDate(String postCreationDateString) {
        return documentDBManager.getPostByDate(postCreationDateString);
    }

    public Post getPostById(String postIdString){
        return documentDBManager.getPostById(postIdString);
    }

    public ArrayList<Post> getPostByOwnerUsername(String ownerPostUsername) {
        return documentDBManager.getPostByOwnerUsername(ownerPostUsername);
    }

    public ArrayList<Post> getPostsByTag(String[] tags){
        return documentDBManager.getPostsByTag(tags);
    }

    public ArrayList<Post> getPostByText(String text){
        return documentDBManager.getPostsByText(text);
    }

    public boolean insertPost(Post newPost){
        boolean insertedPost = documentDBManager.insertPost(newPost);
        graphDBManager.insertPost(newPost);
        return insertedPost;
    }

    public boolean removePost(Post post){
        boolean postRemoved = documentDBManager.removePost(post);
        graphDBManager.removePost(post);
        return postRemoved;
    }

    /*
    --------------------------- ANSWERS ---------------------------
     */

    public boolean insertAnswer(Answer answer, String postIdString){
        documentDBManager.insertAnswer(answer, postIdString);
        graphDBManager.insertAnswer(answer, postIdString);
        return true;
    }

    public boolean removeAnswer(Answer answer, String postIdString){
        documentDBManager.removeAnswer(answer, postIdString);
        graphDBManager.removeAnswer(answer, postIdString);
        return true;
    }

    /*
    --------------------------- VOTES ---------------------------
     */

    public boolean insertRelationVote(String userIdString, String answerIdString, int voteAnswer){
        graphDBManager.insertRelationVote(userIdString, answerIdString, voteAnswer);
        return true;
    }
    public boolean removeRelationVote(String userIdString, String answerIdString){
        graphDBManager.removeRelationVote(userIdString, answerIdString);
        return true;
    }

    /*
    --------------------------- FOLLOWs ---------------------------
     */

    public boolean insertFollowRelationAndUpdate(String userIdFollower, String userIdFollowed){
        documentDBManager.insertUserFollowerAndFollowedRelation(userIdFollower, userIdFollowed);
        graphDBManager.insertFollowRelationAndUpdate(userIdFollower, userIdFollowed);
        return true;
    }
    public boolean removeFollowRelationAndUpdate(String userIdFollower, String userIdFollowed){
        documentDBManager.removeUserFollowerAndFollowedRelation(userIdFollower, userIdFollowed);
        graphDBManager.removeFollowRelationAndUpdate(userIdFollower, userIdFollowed);
        return true;
    }


























}