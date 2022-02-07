package it.unipi.dii.server.databaseDriver;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import javafx.util.Pair;
import org.apache.commons.codec.digest.DigestUtils;
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
    public Map<User, ArrayList<Post>> findMostAnsweredTopUserPosts(){ return graphDBManager.findMostAnsweredTopUserPosts(); }

    public String[] findTopExpertsByTag(String tagName, int numExperts){ return documentDBManager.findTopExpertsByTag(tagName, numExperts); }

    public Map<String, Integer> findMostPopularTags() { return graphDBManager.findMostPopularTags(); }

    public User[] getUsersRank(){ return documentDBManager.getUsersRank(); }

    public HashMap<User, ArrayList<Pair<Post, Integer>>>  findHotTopicsforTopUsers(){ return graphDBManager.findHotTopicsForTopUsers(); }

    public ArrayList<String> getCorrelatedUsers(String username){
        return graphDBManager.getCorrelatedUsers(username);
    }

    public ArrayList<String> getRecommendedUsers(String username, String tagName){ return graphDBManager.getRecommendedUsers(username, tagName); }

    /*
    --------------------------- USERS ---------------------------
     */

    public User getUserDataByUsername(String username){
        return documentDBManager.getUserDataByUsername(username);
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
        graphDBManager.removeUser(user.getDisplayName());
        return documentDBManager.removeUser(user.getDisplayName());
    }


    public boolean updateUserData(User user){
        return documentDBManager.updateUserData(user);
    }

    /*
    --------------------------- POSTS ---------------------------
     */



    public Post getPostById(String postId){
        return documentDBManager.getPostById(postId);
    }

    public ArrayList<Post> getPostByOwnerUsername(String ownerPostUsername) {
        return documentDBManager.getPostByOwnerUsername(ownerPostUsername);
    }


    public ArrayList<Post> getPostsByText(String text){
        return documentDBManager.getPostsByText(text);
    }

    public boolean insertPost(Post newPost){

        String globalPostId = DigestUtils.sha256Hex(newPost.getTitle() + newPost.getCreationDate().toString());
        newPost.setGlobalId(globalPostId);

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

    //TODO: forse qui non è sha256
    public boolean insertAnswer(Answer answer){

        String globalAnswerId = DigestUtils.sha256Hex(answer.getBody() + answer.getCreationDate().toString());
        answer.setAnswerId(globalAnswerId);

        documentDBManager.insertAnswer(answer);
        // postCondition: l'id della risposta e' stato inserito dal documentDBManager
        graphDBManager.insertAnswer(answer);
        return true;
    }

    public boolean removeAnswer(Answer answer){
        documentDBManager.removeAnswer(answer);
        graphDBManager.removeAnswer(answer);
        return true;
    }

    public ArrayList<Answer> getUserAnswer(String username){
        return graphDBManager.findUserAnswers(username);
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

    public boolean checkFollowRelation(String displayName, String displayNameToCheck) {
        return graphDBManager.checkFollowRelation(displayName, displayNameToCheck);
    }

    public ArrayList<String> getUserIdsFollower(String userId) {
        return graphDBManager.getUserIdsFollower(userId);
    }

    public ArrayList<String> getUserIdsFollowed(String userId) {
        return graphDBManager.getUserIdsFollowed(userId);
    }
}
