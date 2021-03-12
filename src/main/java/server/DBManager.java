package server;

import java.util.*;

//classe preposta a ricevere le richieste dal clientManager e a propagarle al documentDB e al graphDB
public class DBManager {

    private DocumentDBManager documentDBManager;
    private GraphDBManager graphDBManager;

    public DBManager(){
        documentDBManager = new DocumentDBManager();
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
        //TODO: questa query è da fare su mongoDB, definire il metodo
        return null;
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

    //TODO: Definire il metodo removeUser nella sua completezza (relazioni, post, e answer dell'utente)
    public boolean removeUser(User user){
        documentDBManager.removeUser(user.getUserId());
        graphDBManager.removeUser(user.getUserId());

        return true;
    }

    public boolean updateUserData(User user){
        return documentDBManager.updateUserData(user);
    }

    /*
    --------------------------- POSTS ---------------------------
     */

    public Post[] getPostByDate(String postCreationDateString) {
        return documentDBManager.getPostByDate(postCreationDateString);
    }

    public Post getPostById(String postIdString){
        return documentDBManager.getPostById(postIdString);
    }

    public Post[] getPostByOwnerUsername(String ownerPostUsername) {
        return documentDBManager.getPostByOwnerUsername(ownerPostUsername);
    }

    public Post[] getPostsByTag(String[] tags){
        return documentDBManager.getPostsByTag(tags);
    }

    public Post[] getPostByText(String text){
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

    public boolean insertFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        graphDBManager.insertFollowRelationAndUpdate(usernameFollower, usernameFollowed);

        return true;
    }
    public boolean removeFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        graphDBManager.removeFollowRelationAndUpdate(usernameFollower, usernameFollowed);

        return true;
    }


























}
