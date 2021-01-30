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

    public void findMostPopularTags(){
        try (Session session = dbConnection.session())
        {
            session.readTransaction(tx -> {
                Result result = tx.run( "MATCH (q:Question)-[:CONTAINS_TAG]->(t:Tag) " +
                                "RETURN t.name as Name, count(*) AS NQuestions " +
                                "ORDER BY NQuestions DESC " +
                                "LIMIT 10");
                Map<String, Integer> tags = new HashMap<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    tags.put(r.get("Name").asString(), r.get("NQusetions").asInt());
                }
                return tags;
            });
        }
    }

    public void findMostVotedAnswers(){
        try (Session session = dbConnection.session())
        {
            session.readTransaction(tx -> {
                Result result = tx.run( "MATCH (:User)-[r:VOTES]->(a:Answer)-[:ANSWERS_TO]->(q:Question) " +
                        "RETURN q.QuestionId as QuestionId, a.answerId as AnswerId, sum(r.VoteTypeId) AS Vote " +
                        "ORDER BY Vote DESC " +
                        "LIMIT 10");
                Map<String, Integer> tags = new HashMap<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    tags.put(r.get("Question").asString(), r.get("Vote").asInt());
                }
                return tags;
            });
        }
    }


    public void getCorrelatedUsers(String username){
        try (Session session = dbConnection.session())
        {
            session.readTransaction((TransactionWork<List<String>>) tx -> {
                Result result = tx.run( "MATCH (u3:User)<-[:FOLLOWS]-(u2:User)<-[:FOLLOWS]-(u:User {userId: $userId}) " +
                                "WHERE u3 <> u and u3 <> u2 //u2 might follow u back, so a cycle is present (u3 <> u), and u3 should not be already followed by u (so u3 <> u2) " +
                                "RETURN distinct u3.displayName AS Username " +
                                "LIMIT 10",
                        parameters( "userId", username) );
                ArrayList<String> users = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    users.add(r.get("Username").asString());
                }
                return users;
            });
        }
    }

    public void getRecommendedUsers(String username, String name){
        try (Session session = dbConnection.session())
        {
            List<String> usersList = session.readTransaction((TransactionWork<List<String>>) tx -> {
                Result result = tx.run( "MATCH (u: User {userId : $userId})-[:POSTS_QUESTION]->(:Question)-[:CONTAINS_TAG]->(t:Tag {name: $name}), " +
                                "(u2: User)-[:POSTS_QUESTION]->(:Question)-[:CONTAINS_TAG]->(t) " +
                                "WHERE u <> u2 " +
                                "RETURN distinct u2.displayName as Username " +
                                "LIMIT 10",
                        parameters("userId", username, "name", name));
                ArrayList<String> users = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    users.add(r.get("Username").asString());
                }
                return users;
            });
        }
    }

    public void insertAnswer(Answer answer){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("CREATE (a:Answer {answerId: $answerId})",
                       parameters("answerId", answer.getAnswerId()));
               return null;
            });
        }
    }

    public void insertFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (fd:User) WHERE fd.userId = $userIdFollowed " +
                                "MATCH (fr:User) WHERE fr.userId = $userIdFollower " +
                                "CREATE (fr)-[:FOLLOWS]->(fd)",
                        parameters("userIdFollowed", usernameFollowed, "userIdFollower", usernameFollower));
                return null;
            });
        }
    }

    public void insertPost(Post post){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx ->{
               tx.run("CREATE (q:Question {questionId: $questionId})",
                       parameters("questionId", post.getPostId()));
               return null;
            });
        }
    }

    public void insertRelationAnswerTo(String answerId, String postId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:Answer) WHERE a.answerId = $answerId" +
                                "MATCH (q:Question) WHERE q.questionId = $questionId" +
                                "Create (a)-[:ANSWERS_TO]->(q)",
                        parameters("answerId", answerId, "questionId", postId));
                return null;
            });
        }
    }

    public void insertRelationContainsTag(String name, String postId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (t:Tag) WHERE t.name = $name" +
                                "MATCH (q:Question) WHERE q.questionId = $questionId" +
                                "Create (q)-[:CONTAINS_TAG]->(t)",
                        parameters("postId", postId, "name", name));
                return null;
            });
        }
    }

    public void insertRelationPostsAnswer(String answerId, User user){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User) WHERE u.userId = $userId" +
                                "MATCH (a:Answer) WHERE a.answerId = $answerId" +
                                "CREATE (u)-[:POSTS_ANSWER]->(a)",
                        parameters("userId", user.getUserId(), "answerId", answerId));
                return null;
            });
        }
    }

    public void insertRelationPostsQuestion(String postId, User user){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User) WHERE u.userId = $userId" +
                                "MATCH (q:Question) WHERE q.questionId = $questionId" +
                                "CREATE (u)-[:POSTS_QUESTION]->(q)",
                        parameters("userId", user.getUserId(), "questionId", postId));
                return null;
            });
        }
    }

    public void insertRelationVote(String answerId, User user, int voto){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User) WHERE u.userId = $userId " +
                                "MATCH (a:Answer) WHERE a.answerId = $answerId " +
                                "CREATE (u)-[:VOTES {voteTypeId: $voteTypeId}]->(a)",
                        parameters("userId", user.getUserId(), "answerId", answerId, "voteTypeId", voto));
                return null;
            });
        }
    }

    public void insertTag(String name){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("CREATE (t:Tag {name: $name})",
                        parameters("name", name));
                return null;
            });
        }
    }

    public void insertUser(User user){
        try (Session session = dbConnection.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "CREATE (u:User {userId: $userId, displayName: $displayName})",
                        parameters( "userId", user.getUserId(), "displayName", user.getDisplayName() ) );
                return null;
            });
        }
    }



    public void removeAnswer(Answer answer){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("MATCH (a:Answer {answerId: $answerId}) " +
                       "DETACH DELETE a",
                       parameters("answerId", answer.getAnswerId()));
               return null;
            });
        }
    }

    public void removeFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (fr:User {userId = $userIdFollower})-[r:FOLLOWS]->(fd:User {userId = $userIdFollowed})" +
                                "DELETE r",
                        parameters("userIdFollower", usernameFollower, "userIdFollowed", usernameFollowed));
                return null;
            });
        }
    }

    public void removePost(Post post){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("MATCH (q:Question {questionId: $questionId}) " +
                       "DETACH DELETE q",
                       parameters("questionId", post.getPostId()));
               return null;
            });
        }
    }

    public void removeRelationAnswerTo(String postId, String answerId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:Answer {answerId = $answerId})-[r:ANSWERS_TO]->(:Question {questionId = $questionId})" +
                                "DELETE r",
                        parameters("answerId", answerId, "postId", postId));
                return null;
            });
        }
    }

    public void removeRelationContainsTag(String postId, String name){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:Question {questionId = $questionId})-[r:CONTAINS_TAG]->(:Tag {name = $name})" +
                                "DELETE r",
                        parameters("questionId", postId, "name", name));
                return null;
            });
        }
    }

    public void removeRelationPostsAnswer(String userId, String answerId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User {userId = $userId})-[r:POSTS_ANSWER]->(:Answer {answerId = $answerId})" +
                                "DELETE r",
                        parameters("userId", userId, "answerId", answerId));
                return null;
            });
        }
    }

    public void removeRelationPostsQuestion(String userId, String postId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User {userId = $userId})-[r:POSTS_QUESTION]->(:Question {questionId = $questionId})" +
                                "DELETE r",
                        parameters("userId", userId, "questionId", postId));
                return null;
            });
        }
    }

    public void removeRelationVote(String userId, String answerId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User {userId = $userId})-[r:VOTES]->(:Answer {answerId = $answerId})" +
                                "DELETE r",
                        parameters("userId", userId, "answerId", answerId));
                return null;
            });
        }
    }

    public void removeTag(String name){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (t:Tag {name: $name})" +
                                "DETACH DELETE t",
                        parameters("name", name));
                return null;
            });
        }
    }

    public void removeUser(String userId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
              tx.run("MATCH (u:User {userId: $userId}) " +
                      "DETACH DELETE u",
                      parameters("userId", userId));
              return null;
            });
        }
    }


}
