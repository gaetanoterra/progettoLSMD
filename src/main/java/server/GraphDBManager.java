package server;

import org.neo4j.driver.*;
import java.util.*;
import static org.neo4j.driver.Values.parameters;

import org.neo4j.driver.Record;


//classe preposta ad effettuare le query del graph database
public class GraphDBManager {

    private Driver dbConnection;

    public GraphDBManager(){
        String uri = "bolt://host-1:7687";
        String user = "neo4j";
        String password = "pseudostackoverdb";
        dbConnection = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void close(){
        dbConnection.close();
    }


    //funzione che effettua la query per trovare i tag più "popolari"
    public Map<String,Integer> findMostPopularTags(){
        try (Session session = dbConnection.session())
        {
            return session.readTransaction(tx -> {
                Map<String, Integer> tags = new HashMap<>();
                tx.run(
                        "MATCH (q:Question)-[:CONTAINS_TAG]->(t:Tag) " +
                        "RETURN t.name as Name, count(*) AS NQuestions " +
                        "ORDER BY NQuestions DESC " +
                        "LIMIT 10; "
                ).stream().forEach(record ->
                        tags.put(record.get("Name").asString(), record.get("NQuestions").asInt())
                );
                return tags;
            });
        }
    }

    //TODO: Su graphdb la query per cercare le risposte più votate non è utilizzata
    //funzione che effettua la query per trovare le risposte più votate
    public void findMostVotedAnswers(){
        try (Session session = dbConnection.session())
        {
            session.readTransaction(tx -> {
                Result result = tx.run( "MATCH (:User)-[r:VOTES]->(a:Answer)-[:ANSWERS_TO]->(q:Question) " +
                        "RETURN q.QuestionId as QuestionId, a.answerId as AnswerId, sum(r.VoteTypeId) AS Vote " +
                        "ORDER BY Vote DESC " +
                        "LIMIT 10; ");
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

    //si potrebbe aggiungere l'immagine del profilo (se inserita nel graph) alle cose da prendere
    //funzione che effettua la query per trovare gli utenti correlati all'utente username
    public String[] getCorrelatedUsers(String username){
        ArrayList<String> users = new ArrayList<>();

        try (Session session = dbConnection.session())
        {
            return (String[]) session.readTransaction((TransactionWork<List<String>>) tx -> {
                Result result = tx.run( "MATCH (u3:User)<-[:FOLLOWS]-(u2:User)<-[:FOLLOWS]-(u:User {userId: $userId}) " +
                                "WHERE u3 <> u and u3 <> u2 //u2 might follow u back, so a cycle is present (u3 <> u), and u3 should not be already followed by u (so u3 <> u2) " +
                                "RETURN distinct u3.displayName AS Username " +
                                "LIMIT 10; ",
                        parameters( "userId", username) );

                while(result.hasNext())
                {
                    Record r = result.next();
                    users.add(r.get("Username").asString());
                }
                return users;
            }).toArray();
        }
    }

    //funzione che effettua la query per trovare gli utenti correlati ad un certo tag
    public String[] getRecommendedUsers(String userId, String tagName){
        try (Session session = dbConnection.session())
        {
            return (String[]) session.readTransaction((TransactionWork<List<String>>) tx -> {
                Result result = tx.run( "MATCH (u: User {userId : $userId})-[:POSTS_QUESTION]->(:Question)-[:CONTAINS_TAG]->(t:Tag {name: $name}), " +
                                "(u2: User)-[:POSTS_QUESTION]->(:Question)-[:CONTAINS_TAG]->(t) " +
                                "WHERE u <> u2 " +
                                "RETURN distinct u2.displayName as Username " +
                                "LIMIT 10; ",
                        parameters("userId", userId, "name", tagName));
                ArrayList<String> users = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    users.add(r.get("Username").asString());
                }
                return users;
            }).toArray();
        }
    }

    //funzione che effettua la query per per inserire il nodo Answer
    public void insertAnswer(Answer answer, String postIdString){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("CREATE (a:Answer {answerId: $answerId}); ",
                       parameters("answerId", answer.getAnswerId()));
               return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:Answer {answerId: $answerId}), " +
                                "(q:Question {questionId: $questionId}) " +
                                "CREATE (a)-[:ANSWERS_TO]->(q); ",
                        parameters("answerId", answer.getAnswerId(), "questionId", postIdString));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {userId: $userId}), " +
                                "(a:Answer {answerId: $answerId}) " +
                                "CREATE (u)-[:POSTS_ANSWER]->(a); ",
                        parameters("userId", answer.getOwnerUserId(), "answerId", answer.getAnswerId()));
                return null;
            });
        }

    }

    //funzione che effettua la query per inserire la relazione Follow tra due username
    public void insertFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (fd:User {userId: $userIdFollowed), " +
                                "(fr:User {userId: $userIdFollower) " +
                                "CREATE (fr)-[:FOLLOWS]->(fd); ",
                        parameters("userIdFollowed", usernameFollowed, "userIdFollower", usernameFollower));
                return null;
            });
        }
    }

    //funzione che effettua la query per inserire il nodo Post
    public void insertPost(Post post){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx ->{
               tx.run("CREATE (q:Question {questionId: $questionId}); ",
                       parameters("questionId", post.getPostId()));
               return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {userId: $userId}), " +
                                "(q:Question {questionId: $questionId}) " +
                                "CREATE (u)-[:POSTS_QUESTION]->(q); ",
                        parameters("userId", post.getOwnerUserId(), "questionId", post.getPostId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (q:Question {questionId: $questionId}) " +
                                "FOREACH (tagName IN $tagList | MERGE (q)-[:CONTAINS_TAG]->(t:Tag {name: tagName})); ",
                        parameters("questionId", post.getPostId() ,"tagList", post.getTags()));
                return null;
            });
        }
    }


    //funzione che effettua la query per inserire la relazione Contains_tag tra Post e Tag
    public void insertRelationContainsTag(String postId, String name){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (t:Tag {name: $name}), " +
                                "(q:Question {questionId: $questionId}) " +
                                "CREATE (q)-[:CONTAINS_TAG]->(t); ",
                        parameters("postId", postId, "name", name));
                return null;
            });
        }
    }


    //funzione che effettua la query per inserire la relazione Votes tra Answer e Post
    public void insertRelationVote(String userIdString, String answerIdString, int voteAnswer){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {userId: $userId}), " +
                                "(a:Answer {answerId: $answerId}) " +
                                "CREATE (u)-[:VOTES {voteTypeId: $voteTypeId}]->(a); ",
                        parameters("userId", userIdString, "answerId", answerIdString, "voteTypeId", voteAnswer));
                return null;
            });
        }
    }

    //TODO: Su graphdb la query per inserire un tag non è utilizzata
    //funzione che effettua la query per inserire il nodo Tag
    public void insertTag(String name){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("CREATE (t:Tag {name: $name}); ",
                        parameters("name", name));
                return null;
            });
        }
    }

    //funzione che effettua la query per inserire il nodo User
    public void insertUser(User user){
        try (Session session = dbConnection.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "CREATE (u:User {userId: $userId, displayName: $displayName}); ",
                        parameters( "userId", user.getUserId(), "displayName", user.getDisplayName() ) );
                return null;
            });
        }
    }

    //funzione che effettua la query per rimuovere il nodo Answer
    public void removeAnswer(Answer answer, String postIdString){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("MATCH (a:Answer {answerId: $answerId}) " +
                       "DETACH DELETE a; ",
                       parameters("answerId", answer.getAnswerId()));
               return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:Answer {answerId = $answerId})-[r:ANSWERS_TO]->(:Question {questionId = $questionId}) " +
                                "DELETE r; ",
                        parameters( "questionId", postIdString, "answerId", answer.getAnswerId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User {userId = $userId})-[r:POSTS_ANSWER]->(:Answer {answerId = $answerId}) " +
                                "DELETE r; ",
                        parameters("userId", answer.getOwnerUserId(), "answerId", answer.getAnswerId()));
                return null;
            });
        }
    }

    //funzione che effettua la query per rimuovere la relazione Follows tra due utenti
    public void removeFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (fr:User {userId = $userIdFollower})-[r:FOLLOWS]->(fd:User {userId = $userIdFollowed}) " +
                                "DELETE r; ",
                        parameters("userIdFollower", usernameFollower, "userIdFollowed", usernameFollowed));
                return null;
            });
        }
    }


    public void removePost(Post post){
        /*
        Rimuovere le risposte su graph db a quel post (con annesse relazioni, usando detach delete questa ultima parte è automatica)
        Rimuovere il post su graph db (con annesse relazioni, come prima)
        Se uno o più tag rimangono senza post collegati, rimuovere anche quelli
         */
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:Answer)-[:ANSWERS_TO]->(:Question {questionId: $questionId}) " +
                                "DETACH DELETE a; ",
                        parameters("questionId", post.getPostId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("MATCH (q:Question {questionId: $questionId}) " +
                       "DETACH DELETE q; ",
                       parameters("questionId", post.getPostId()));
               return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (t:Tag) " +
                                "WHERE t.name in $tagList AND WHERE NOT (t)<-[:CONTAINS_TAG]-() " +
                                "DELETE t; ",
                        parameters("tagList", post.getTags()));
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
                tx.run("MATCH (u:User {userId = $userId})-[:FOLLOWS]-(:User), " +
                "(u)-[:POSTS_QUESTION]->(q:Question), " +
                "(u)-[:POSTS_ANSWER]->(a:Answer), " +
                "DETACH DELETE u, q, a;",
              parameters("userId", userId));
              return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (t:Tag) "+
                        "WHERE NOT (t)<-[:CONTAINS_TAG]-() " +
                        "DELETE t");
                return null;
            });
        }
    }


}