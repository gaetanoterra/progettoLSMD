package it.unipi.dii.server.databaseDriver;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import javafx.util.Pair;

import java.time.Instant;
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
    //TODO: questa va fatta con il graphDB
    public Map<User, Post[]> findMostAnsweredTopUserPosts(){ return documentDBManager.findMostAnsweredTopUserPosts(); }

    public String[] findTopExpertsByTag(String tagName, int numExperts){ return documentDBManager.findTopExpertsByTag(tagName, numExperts); }

    //TODO: Non esiste un messaggio per ottenere i tag popolari tra gli utenti che stanno in una location. Creare un opcode e un messagio per questo
    //per i most popular tags per lcation inserire nella pagina delle analytics una piechart che mostra i top 10, con una casella di testo per modificare la location
    public String[] findMostPopularTagsByLocation(String location, int numTags){ return documentDBManager.findMostPopularTagsByLocation(location, numTags); }

    //TODO: Non esiste un messaggio per ottenere i tag più popolari in generale. Creare un opcode e un messagio per questo
    //per i most popular tags inserire nella pagina delle analytics una piechart che mostra i top 10
    public Map<String, Integer> findMostPopularTags() { return graphDBManager.findMostPopularTags(); }

    //TODO: Non esiste un messaggio per ottenere il ranking degli utenti. Creare un opcode e un messagio per questo
    public User[] getUsersRank(){ return documentDBManager.getUsersRank(); }

    public Map<User, Pair<String, Integer>[]> findHotTopicsforTopUsers(){ return documentDBManager.findHotTopicsForTopUsers(); }

    //TODO: Non esiste un messaggio per ottenere gli utenti correlati. Creare un opcode e un messagio per questo
    //restituisco gli username degli utenti (nel graphDB ci sono solo quelli), poi quando seleziono uno specifico utente chiamo la getUserByUsername
    public String[] getCorrelatedUsers(String username){
        return graphDBManager.getCorrelatedUsers(username);
    }

    //TODO: Non esiste un messaggio per ottenere gli utenti raccomandati. Creare un opcode e un messagio per questo
    //restituisco gli username degli utenti (nel graphDB ci sono solo quelli), poi quando seleziono uno specifico utente chiamo la getUserByUsername
    public String[] getRecommendedUsers(String username, String tagName){ return graphDBManager.getRecommendedUsers(username, tagName); }

    /*
    --------------------------- USERS ---------------------------
     */


    public User getUserData(String username){
        return documentDBManager.getUserData(username);
    }

    public boolean insertUser(User newUser){
        if(!documentDBManager.checkUser(newUser.getDisplayName())) {
            newUser.setCreationDate(Instant.now().toEpochMilli())
                    .setLastAccessDate(Instant.now().toEpochMilli())
                    .setFollowedNumber(0)
                    .setFollowersNumber(0)
                    .setReputation(0);
            boolean insertedUser = documentDBManager.insertUser(newUser);
            if (insertedUser) {
                graphDBManager.insertUser(newUser);
            }
            return insertedUser;
        }
        else
            return false;
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

    public Post getPostById(String postId){
        return documentDBManager.getPostById(postId);
    }

    public ArrayList<Post> getPostByOwnerUsername(String ownerPostUsername) {
        return documentDBManager.getPostByOwnerUsername(ownerPostUsername);
    }

    public ArrayList<Post> getPostsByTag(String[] tags){
        return documentDBManager.getPostsByTag(tags);
    }

    public ArrayList<Post> getPostsByText(String text){
        return documentDBManager.getPostsByText(text);
    }

    public boolean insertPost(Post newPost){
        boolean insertedPost = documentDBManager.insertPost(newPost);
        if (insertedPost) {
            // in insertPost il post è stato modificato aggiungendo l'id
            graphDBManager.insertPost(newPost);
        }

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

    public boolean insertAnswer(Answer answer, String postId){
        documentDBManager.insertAnswer(answer, postId);
        // postCondition: l'id della risposta e' stato inserito dal documentDBManager
        graphDBManager.insertAnswer(answer, postId);
        return true;
    }

    public boolean removeAnswer(Answer answer, String postId){
        documentDBManager.removeAnswer(answer, postId);
        graphDBManager.removeAnswer(answer, postId);
        return true;
    }

    public ArrayList<Post> getAnswers(String displayName){
        return documentDBManager.getAnswers(displayName);
    }

    /*
    --------------------------- VOTES ---------------------------
     */
    // se voto su/giu, e non ho votato -> punteggio + voto, e tipo voto registrato
    // se voto su/giu e ho votato -> altra situazione, e tipo voto aggiornato
    public boolean insertRelationVote(String userId, String answerId, String postId, int voteAnswer){
        int previousVote = graphDBManager.getVote(userId, answerId);
        if (previousVote == 0) {
            //niente voto -> registro normalmente
            documentDBManager.updateVotesAnswer(postId, answerId, voteAnswer);
            graphDBManager.insertRelationVote(userId, answerId, voteAnswer);
        }
        else {
            // esiste già un voto -> eliminare quello precedente e inserire quello nuovo
            // se previousVote == voto -> eliminare il vecchio voto (annulla il vecchio voto)
            // se previousVote == -1 e voto == +1 -> voto answer += 2 e aggiorno relazione con nuovo voto
            // se previousVote == 1 e voto == -1 -> voto answer += -2 e aggiorno relazione con nuovo voto
            if (previousVote == voteAnswer) {
                documentDBManager.updateVotesAnswer(postId, answerId, -voteAnswer);
                graphDBManager.removeRelationVote(userId, answerId);
            }
            else {
                documentDBManager.updateVotesAnswer(postId, answerId, voteAnswer - previousVote);
                graphDBManager.insertRelationVote(userId, answerId, voteAnswer);
            }
        }

        return true;
    }
    public boolean removeRelationVote(String userId, String answerId, String postId, int voteAnswer){
        //TODO: L'operazione DELETE non viene utilzzata, valutare se mantenere questo metodo
        documentDBManager.updateVotesAnswer(postId, answerId, voteAnswer);
        graphDBManager.removeRelationVote(userId, answerId);
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

    public ArrayList<String> getFollowers(String displayName){
        return graphDBManager.getUserDisplayNameFollower(displayName);
    }

}
