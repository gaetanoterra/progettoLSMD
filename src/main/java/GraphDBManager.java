import org.neo4j.driver.*;
import java.util.*;
import static org.neo4j.driver.Values.parameters;

public class GraphDBManager {

    private Driver dbConnection;

    public GraphDBManager(String uri, String user, String password){
        dbConnection = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void close() throws Exception
    {
        dbConnection.close();
    }

    public Map<User, Post> findMostAnsweredTopUserPosts(){

    }

    public User[] getCorrelatedUsers(String username){

    }

    public User[] getRecommendedUsers(String username){

    }

    public boolean insertAnswer(Answer answer){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("CREATE (a:Answer {answerId: $answerId})",
                       parameters("answerId", answer.getAnswerId()));
               return null;
            });
        }
        return true;
    }

    public boolean insertFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){

    }

    public boolean insertPost(Post post){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx ->{
               tx.run("CREATE (q:Question {questionId: $questionId})",
                       parameters("questionId", post.getPostId()));
               return null;
            });
        }
        return true;
    }

    public boolean insertUser(User user){
        try (Session session = dbConnection.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "CREATE (u:User {userId: $userId, displayName: $displayName})",
                        parameters( "userId", user.getUserId(), "displayName", user.getDisplayName() ) );
                return null;
            });
        }
        return true;
    }

    public boolean insertVote(int answerId, User user, int voto){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("MATCH (u:User) WHERE u.userId = $userId "+
                        "MATCH (a:Answer) WHERE a.answerId = $answerId "+
                        "CREATE (u)-[:VOTES {voteTypeId: $voteTypeId}]->(a)",
                       parameters("userId", user.getUserId(), "answerId", answerId, "voteTypeId", voto));
               return null;
            });
        }
        return true;
    }

    public boolean removeAnswer(Answer answer){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("MATCH (a:Answer {answerId: $answerId}) " +
                       "DETACH DELETE a",
                       parameters("answerId", answer.getAnswerId()));
               return null;
            });
        }
        return true;
    }

    public boolean removeFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){

    }

    public boolean removePost(Post post){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("MATCH (q:Question {questionId: $questionId}) " +
                       "DETACH DELETE q",
                       parameters("questionId", post.getPostId()));
               return null;
            });
        }
        return true;
    }

    public boolean removeUser(String userId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
              tx.run("MATCH (u:User {userId: $userId}) " +
                      "DETACH DELETE u",
                      parameters("userId", userId));
              return null;
            });
        }
        return true;
    }
}
