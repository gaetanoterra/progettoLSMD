package it.unipi.dii.server.databaseDriver;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import javafx.util.Pair;
import org.javatuples.Triplet;

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


    public User getUserDataById(String userId){
        return documentDBManager.getUserDataById(userId);
    }

    public User getUserDataByUsername(String username){
        return documentDBManager.getUserDataByUsername(username);
    }

    public boolean insertUser(User newUser){
        newUser.setCreationDate(Instant.now().toEpochMilli())
                .setLastAccessDate(Instant.now().toEpochMilli())
                .setFollowedNumber(0)
                .setFollowersNumber(0)
                .setReputation(0);
        boolean insertedUser = documentDBManager.insertUser(newUser);
        if(insertedUser) {
            graphDBManager.insertUser(newUser);
        }
        return insertedUser;
    }

    public boolean removeUser(User user){
        String userId = user.getUserId();
        //prima recupero gli id dei follower e dei followed dell'utente dal graph db
        List<String> userIdsFollower = graphDBManager.getUserIdsFollower(userId);
        List<String> userIdsFollowed = graphDBManager.getUserIdsFollowed(userId);
        //poi recupero gli id delle risposte dove l'utente ha votato
        List<Triplet<String, String, Integer>> postIdsAnswer = graphDBManager.getAnswersVotedByUser(userId);
        //poi posso eliminare l'utente dal graph db
        graphDBManager.removeUser(userId);
        //e infine posso eliminare l'utente dal document db (con gestione attributi utente ridondanti e dei voti)
        return documentDBManager.removeUser(userId, userIdsFollower, userIdsFollowed, postIdsAnswer);
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

    /*
    --------------------------- VOTES ---------------------------
     */
    // se voto su/giu, e non ho votato -> punteggio + voto, e tipo voto registrato
    // se voto su/giu e ho votato -> altra situazione, e tipo voto aggiornato
    public boolean insertRelationVote(String userIdVoter, String answerId, String postId, int voteAnswer){
        int previousVote = graphDBManager.getVote(userIdVoter, answerId);
        if (previousVote == 0) {
            //niente voto -> registro normalmente
            documentDBManager.updateVotesAnswerAndReputation(postId, answerId, voteAnswer);
            graphDBManager.insertRelationVote(userIdVoter, answerId, voteAnswer);
        }
        else {
            // esiste già un voto -> eliminare quello precedente e inserire quello nuovo
            // se previousVote == voto -> eliminare il vecchio voto (annulla il vecchio voto)
            // se previousVote == -1 e voto == +1 -> voto answer += 2 e aggiorno relazione con nuovo voto
            // se previousVote == 1 e voto == -1 -> voto answer += -2 e aggiorno relazione con nuovo voto
            if (previousVote == voteAnswer) {
                documentDBManager.updateVotesAnswerAndReputation(postId, answerId, -voteAnswer);
                graphDBManager.removeRelationVote(userIdVoter, answerId);
            }
            else {
                documentDBManager.updateVotesAnswerAndReputation(postId, answerId, voteAnswer - previousVote);
                graphDBManager.insertRelationVote(userIdVoter, answerId, voteAnswer);
            }
        }

        return true;
    }
    public boolean removeRelationVote(String userId, String answerId, String postId, int voteAnswer){
        //TODO: L'operazione DELETE non viene utilzzata, valutare se mantenere questo metodo e aggiornarlo
        documentDBManager.updateVotesAnswerAndReputation(postId, answerId, voteAnswer);
        graphDBManager.removeRelationVote(userId, answerId);
        return true;
    }

    /*
    --------------------------- FOLLOWs ---------------------------
     */

    public boolean insertFollowRelationAndUpdate(String userIdFollower, String userIdFollowed){
        graphDBManager.insertFollowRelationAndUpdate(userIdFollower, userIdFollowed);
        documentDBManager.insertUserFollowerAndFollowedRelation(userIdFollower, userIdFollowed);
        return true;
    }
    public boolean removeFollowRelationAndUpdate(String userIdFollower, String userIdFollowed){
        graphDBManager.removeFollowRelationAndUpdate(userIdFollower, userIdFollowed);
        documentDBManager.removeUserFollowerAndFollowedRelation(userIdFollower, userIdFollowed);
        return true;
    }

}
