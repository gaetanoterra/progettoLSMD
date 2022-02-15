package it.unipi.dii.server.databaseDriver;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import javafx.util.Pair;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.time.Instant;
import java.util.*;

//classe preposta a ricevere le richieste dal clientManager e a propagarle al documentDB e al graphDB
public class DBManager {
    private final static Logger LOGGER = Logger.getLogger(DBManager.class.getName());
    private DocumentDBManager documentDBManager;
    private GraphDBManager graphDBManager;
    private final String LOGGER_PROPERTIES = "/logging.properties";

    private void loadLoggerConfiguration() {
        try {
            LogManager.getLogManager().readConfiguration(DBManager.class.getResourceAsStream(LOGGER_PROPERTIES));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load the logger configuration.");
        }
        LOGGER.log(Level.INFO, "logger configuration loaded succesfully");
    }
    public DBManager(){
       this(DBExecutionMode.LOCAL);
    }

    public DBManager(DBExecutionMode dbe){
        documentDBManager = new DocumentDBManager(dbe);
        graphDBManager = new GraphDBManager(dbe);
        loadLoggerConfiguration();
    }

    public void close(){
        this.documentDBManager.close();
        this.graphDBManager.close();
    }

    /*
    --------------------------- ANALYTICS ---------------------------
     */
    public HashMap<User, ArrayList<Post>> findMostAnsweredTopUserPosts(){ return graphDBManager.findMostAnsweredTopUserPosts(); }

    public String[] findTopExpertsByTag(String tagName, int numExperts){ return documentDBManager.findTopExpertsByTag(tagName, numExperts); }

    public Map<String, Integer> findMostPopularTags() { return graphDBManager.findMostPopularTags(); }

    public User[] getUsersRank(){ return documentDBManager.getUsersRank(); }

    public HashMap<User, ArrayList<Pair<Post, Integer>>>  findHotTopicsforTopUsers(){ return graphDBManager.findHotTopicsForTopUsers(); }

    public ArrayList<User> getCorrelatedUsers(String username){
        return graphDBManager.getCorrelatedUsers(username);
    }

    public ArrayList<User> getRecommendedUsers(String username, String tagName){ return graphDBManager.getRecommendedUsers(username, tagName); }

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
            boolean insertedUserInMongoDB = documentDBManager.insertUser(newUser);
            if (!insertedUserInMongoDB) {
                LOGGER.log(Level.SEVERE, "Faield to insert User in MongoDB " + newUser);
            }
            boolean insertedUserInNeo4J = graphDBManager.insertUser(newUser);
            if(!insertedUserInNeo4J){
                LOGGER.log(Level.SEVERE, "Faield to insert User in Neo4j  " + newUser);
            }
            return insertedUserInMongoDB && insertedUserInNeo4J ;
        }
        else
            return false;
    }

    public boolean removeUser(User user){
        boolean removedUserMongoDB = graphDBManager.removeUser(user.getDisplayName());
        if (!removedUserMongoDB) {
            LOGGER.log(Level.SEVERE, "Faield to remove User in MongoDB " + user);
        }
        if(!documentDBManager.removeUser(user.getDisplayName())) {
            LOGGER.log(Level.SEVERE, "Faield to remove User in Neo4j " + user);
            return false;
        }
        return removedUserMongoDB;
    }


    public boolean updateUserData(User user){
         if(!documentDBManager.updateUserData(user)){
             LOGGER.log(Level.SEVERE, "Faield to update User data in MongoDB " + user);
             return false;
         }
         return true;
    }

    /*
    --------------------------- POSTS ---------------------------
     */



    public Post getPostById(String globalPostId){
        return documentDBManager.getPostById(globalPostId);
    }

    public ArrayList<Post> getPostsByOwnerUsername(String ownerPostUsername) {
        return documentDBManager.getPostsByOwnerUsername(ownerPostUsername);
    }


    public ArrayList<Post> getPostsByText(String text){
        return documentDBManager.getPostsByText(text);
    }

    public boolean insertPost(Post newPost){

        String globalPostId = DigestUtils.sha256Hex(newPost.getTitle() + newPost.getCreationDate().toString());
        newPost.setGlobalId(globalPostId);

        boolean insertedPostInMongo = documentDBManager.insertPost(newPost);
        if (!insertedPostInMongo) {
            LOGGER.log(Level.SEVERE, "Failed to insert post in MongoDB " + newPost);
        }

        if( !graphDBManager.insertPost(newPost)){
            LOGGER.log(Level.SEVERE, "Failed to insert post in Neo4j " + newPost);
            return false;
        }

        return insertedPostInMongo;
    }

    public boolean removePost(Post post){
        boolean postRemoved = documentDBManager.removePost(post);
        graphDBManager.removePost(post);
        return postRemoved;
    }

    /*
    --------------------------- ANSWERS ---------------------------
     */

    public boolean insertAnswer(Answer answer){

        String globalAnswerId = DigestUtils.sha1Hex(answer.getBody() + answer.getCreationDate().toString());
        answer.setAnswerId(globalAnswerId);

         boolean insertedAnswerInMongo = documentDBManager.insertAnswer(answer);
         if(!insertedAnswerInMongo)
             LOGGER.log(Level.SEVERE, "Failed to insert post in MongoDB " + answer);

        // postCondition: l'id della risposta e' stato inserito dal documentDBManager
        boolean insertedAnswerInNeo4j = graphDBManager.insertAnswer(answer);
        if(!insertedAnswerInNeo4j)
            LOGGER.log(Level.SEVERE, "Failed to insert post in Neo4j " + answer);

        return true;
    }

    public boolean removeAnswer(Answer answer){

        boolean removedAnswerInMongo = documentDBManager.removeAnswer(answer);
        boolean removedAnswerInNeo4j = graphDBManager.removeAnswer(answer);
        if(!removedAnswerInMongo)
            LOGGER.log(Level.SEVERE, "Failed to remove post in MongoDB " + answer);

        if(!removedAnswerInNeo4j)
            LOGGER.log(Level.SEVERE, "Failed to remove post in Neo4J " + answer);

        return removedAnswerInMongo && removedAnswerInNeo4j;
    }

    public ArrayList<Answer> getUserAnswer(String username){
        return graphDBManager.findUserAnswers(username);
    }

    /*
    --------------------------- VOTES ---------------------------
     */
    // se voto su/giu, e non ho votato -> punteggio + voto, e tipo voto registrato
    // se voto su/giu e ho votato -> altra situazione, e tipo voto aggiornato
    public boolean insertRelationVote(String userDisplayNameVoter, String answerId, String postId, int voteAnswer){
        int previousVote = graphDBManager.getVote(userDisplayNameVoter, answerId);
        boolean updatedVoteInMongo ;
        boolean updatedVoteInNeo4j ;
        if (previousVote == 0) {
            //niente voto -> registro normalmente
            updatedVoteInMongo = documentDBManager.updateVotesAnswerAndReputation(postId, answerId, voteAnswer);
            updatedVoteInNeo4j = graphDBManager.insertRelationVote(userDisplayNameVoter, answerId, voteAnswer);
        }
        else {
            // esiste già un voto -> eliminare quello precedente e inserire quello nuovo
            // se previousVote == voto -> eliminare il vecchio voto (annulla il vecchio voto)
            // se previousVote == -1 e voto == +1 -> voto answer += 2 e aggiorno relazione con nuovo voto
            // se previousVote == 1 e voto == -1 -> voto answer += -2 e aggiorno relazione con nuovo voto
            if (previousVote == voteAnswer) {
                updatedVoteInMongo = documentDBManager.updateVotesAnswerAndReputation(postId, answerId, -voteAnswer);
                updatedVoteInNeo4j = graphDBManager.removeRelationVote(userDisplayNameVoter, answerId);
            }
            else {
                updatedVoteInMongo = documentDBManager.updateVotesAnswerAndReputation(postId, answerId, voteAnswer - previousVote);
                updatedVoteInNeo4j = graphDBManager.insertRelationVote(userDisplayNameVoter, answerId, voteAnswer);
            }
        }
        if(!updatedVoteInMongo){
            LOGGER.log(Level.SEVERE,"Vote of " + userDisplayNameVoter + " on answer:" + answerId + " not inserted in MongoDB" );
        }
        if(!updatedVoteInNeo4j){
            LOGGER.log(Level.SEVERE,"Vote of " + userDisplayNameVoter + " on answer:" + answerId + " not inserted in Neo4J" );
        }
        return updatedVoteInMongo && updatedVoteInNeo4j;
    }

    public boolean removeRelationVote(String displayName, String answerId, String postId, int voteAnswer){
        boolean updatedVoteInMongo = documentDBManager.updateVotesAnswerAndReputation(postId, answerId, voteAnswer);
        boolean updatedVoteInNeo4j = graphDBManager.removeRelationVote(displayName, answerId);
        if(!updatedVoteInMongo){
            LOGGER.log(Level.SEVERE,"Vote of " + displayName + " on answer:" + answerId + " not removed in MongoDB" );
        }
        if(!updatedVoteInNeo4j){
            LOGGER.log(Level.SEVERE,"Vote of " + displayName + " on answer:" + answerId + " not removed in Neo4j" );
        }
        return updatedVoteInMongo && updatedVoteInNeo4j;
    }

    /*
    --------------------------- FOLLOWs ---------------------------
     */

    public boolean insertFollowRelationAndUpdate(String userDisplayNameFollower, String userDisplayNameFollowed){
        boolean updatedVoteInNeo4j = graphDBManager.insertFollowRelationAndUpdate(userDisplayNameFollower, userDisplayNameFollowed);
        boolean updatedVoteInMongo = documentDBManager.insertUserFollowerAndFollowedRelation(userDisplayNameFollower, userDisplayNameFollowed);
        if(!updatedVoteInMongo){
            LOGGER.log(Level.SEVERE,"Failed insertion of follow relation bewteen " + userDisplayNameFollower + " and " + userDisplayNameFollowed + " in MongoDB" );
        }
        if(!updatedVoteInNeo4j){
            LOGGER.log(Level.SEVERE,"Failed insertion of follow relation bewteen " + userDisplayNameFollower + " and " + userDisplayNameFollowed + " in Neo4j" );
        }
        return updatedVoteInMongo && updatedVoteInNeo4j;
    }

    public boolean removeFollowRelationAndUpdate(String userDisplayNameFollower, String userDisplayNameFollowed){
        boolean updatedVoteInNeo4j = graphDBManager.removeFollowRelationAndUpdate(userDisplayNameFollower, userDisplayNameFollowed);
        boolean updatedVoteInMongo = documentDBManager.removeUserFollowerAndFollowedRelation(userDisplayNameFollower, userDisplayNameFollowed);
        if(!updatedVoteInMongo){
            LOGGER.log(Level.SEVERE,"Failed removal of follow relation bewteen " + userDisplayNameFollower + " and " + userDisplayNameFollowed + " in MongoDB" );
        }
        if(!updatedVoteInNeo4j){
            LOGGER.log(Level.SEVERE,"Failed removal of follow relation bewteen " + userDisplayNameFollower + " and " + userDisplayNameFollowed + " in Neo4j" );
        }
        return updatedVoteInMongo && updatedVoteInNeo4j;
    }

    public boolean checkFollowRelation(String displayName, String displayNameToCheck) {
        return graphDBManager.checkFollowRelation(displayName, displayNameToCheck);
    }

    public ArrayList<User> getUserIdsFollower(String userId) {
        return graphDBManager.getUserIdsFollower(userId);
    }

    public ArrayList<User> getUserIdsFollowed(String userId) {
        return graphDBManager.getUserIdsFollowed(userId);
    }
}
