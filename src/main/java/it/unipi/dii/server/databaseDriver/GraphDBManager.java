package it.unipi.dii.server.databaseDriver;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import org.neo4j.driver.*;
import java.util.*;
import static org.neo4j.driver.Values.parameters;

import org.neo4j.driver.Record;


//classe preposta ad effettuare le query del graph database
public class GraphDBManager {

    private final Driver dbConnection;

    public GraphDBManager(){
        String uri = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "pseudostackoverdb";
        dbConnection = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void close(){
        dbConnection.close();
    }


    //funzione che effettua la query per trovare i tag più "popolari"
    public Map<String,Integer> findMostPopularTags(){
        try (Session session = dbConnection.session()){
            Map<String, Integer> tags = new HashMap<>();

            List<Record> records = session.readTransaction(new TransactionWork<List<Record>>() {
                @Override
                public List<Record> execute (Transaction tx){
                    return tx.run("MATCH (q:Question)-[:CONTAINS_TAG]->(t:Tag) " +
                                    "RETURN t.name as Name, count(*) AS NQuestions " +
                                    "ORDER BY NQuestions DESC " +
                                    "LIMIT 10").list();
                }
            });

            for(final Record record : records){
                tags.put(record.get("Name").asString(), record.get("NQuestions").asInt());
            }
            return tags;
        }

        /*try (Session session = dbConnection.session())
        {
            return session.readTransaction(tx -> {
                Map<String, Integer> tags = new HashMap<>();
                tx.run(
                        "MATCH (q:Question)-[:CONTAINS_TAG]->(t:Tag) " +
                        "RETURN t.tagNames as Name, count(*) AS NQuestions " +
                        "ORDER BY NQuestions DESC " +
                        "LIMIT 10; "
                ).stream().forEach(record ->
                        tags.put(record.get("Name").asString(), record.get("NQuestions").asInt())
                );
                return tags;
            });
        }*/
    }

    //TODO: Su graphdb la query per cercare le risposte più votate non è utilizzata
    //funzione che effettua la query per trovare le risposte più votate
    public void findMostVotedAnswers(){
        try (Session session = dbConnection.session())
        {
            session.readTransaction(tx -> {
                Result result = tx.run( "MATCH (:User)-[r:VOTE]->(a:Answer)-[:ANSWERS_TO]->(q:Question) " +
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

    public ArrayList<Post> findUserPosts(){
        try (Session session = dbConnection.session())
        {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                                        MATCH (u:User{userId:"29"})-[:POSTS_QUESTION]->(q: Question) <-[:BELONGS_TO]-
                                        (a:Answer) RETURN q.Title as title, count(a) as number_of_answers
                                        """);
                ArrayList<Post> posts = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    posts.add( new Post().setTitle(r.get("title").asString())
                                         .setAnswersNumber(r.get("number_of_answers").asInt())
                    );
                }
                return posts;
            });
        }
    }

    public ArrayList<Answer> findUserAnswers(){
        try (Session session = dbConnection.session())
        {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                                        MATCH (u:User{userId:"29"})-[:ANSWERS_WITH]->(a:Answer)<-[v:VOTE]-(uv:User)
                                        RETURN a.body as body, sum(v.VoteTypeId) as score ORDER BY score DESC
                                        """);
                ArrayList<Answer> answers = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    answers.add( new Answer(r.get("body").asString()).setScore(r.get("score").asInt()));
                 }
                return answers;
            });
        }
    }
    //si potrebbe aggiungere l'immagine del profilo (se inserita nel graph) alle cose da prendere
    //funzione che effettua la query per trovare gli utenti correlati all'utente username
    public String[] getCorrelatedUsers(String username){
        ArrayList<String> users = new ArrayList<>();

        try (Session session = dbConnection.session())
        {  //u2 might follow u back, so a cycle is present (u3 <> u), and u3 should not be already followed by u (so u3 <> u2)
            return (String[]) session.readTransaction((TransactionWork<List<String>>) tx -> {
                Result result = tx.run( "MATCH (u3:User)<-[:FOLLOW]-(u2:User)<-[:FOLLOW]-(u:User {userId: $userId}) " +
                                "WHERE u3 <> u and u3 <> u2  " +
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


    public ArrayList<String> getUserIdsFollower(String userId) {
        try(Session session = dbConnection.session()){
            return session.writeTransaction(tx -> {
                ArrayList<String> userIdsFollower = new ArrayList<>();
                tx.run("MATCH (fr:User)-[:FOLLOW]->(fd:User {userId: $userIdFollowed}) " +
                                "RETURN fr.userId as userIdFollower ",
                        parameters("userIdFollowed", userId))
                .stream().forEach(record ->
                    userIdsFollower.add(record.get("userIdFollower").asString())
                );
                return userIdsFollower;
            });
        }
    }

    public ArrayList<String> getUserIdsFollowed(String userId) {
        try(Session session = dbConnection.session()){
            return session.writeTransaction(tx -> {
                ArrayList<String> userIdsFollowed = new ArrayList<>();
                tx.run("MATCH (fr:User {userId: $userIdFollower})-[:FOLLOW]->(fd:User) " +
                                "RETURN fd.userId as userIdFollowed ",
                        parameters("userIdFollower", userId))
                        .stream().forEach(record ->
                        userIdsFollowed.add(record.get("userIdFollowed").asString())
                );
                return userIdsFollowed;
            });
        }
    }

    //funzione che effettua la query per per inserire il nodo Answer
    public void insertAnswer(Answer answer, String postId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("CREATE (a:Answer {answerId: $answerId}); ",
                       parameters("answerId", answer.getAnswerId()));
               return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:Answer {answerId: $answerId}), " +
                                "(q:Question {QuestionId: $questionId}) " +
                                "CREATE (a)-[:ANSWERS_TO]->(q); ",
                        parameters("answerId", answer.getAnswerId(), "questionId", postId));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {userId: $userId}), " +
                                "(a:Answer {answerId: $answerId}) " +
                                "CREATE (u)-[:ANSWERS_WITH]->(a); ",
                        parameters("userId", answer.getOwnerUserId(), "answerId", answer.getAnswerId()));
                return null;
            });
        }

    }

    //funzione che effettua la query per inserire la relazione Follow tra due username
    public void insertFollowRelationAndUpdate(String userIdFollower, String userIdFollowed){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (fd:User {userId: $userIdFollowed), " +
                                "(fr:User {userId: $userIdFollower) " +
                                "CREATE (fr)-[:FOLLOW]->(fd); ",
                        parameters("userIdFollowed", userIdFollowed, "userIdFollower", userIdFollower));
                return null;
            });
        }
    }

    //funzione che effettua la query per inserire il nodo Post
    public void insertPost(Post post){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx ->{
               tx.run("CREATE (q:Question {QuestionId: $questionId}); ",
                       parameters("questionId", post.getPostId()));
               return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {userId: $userId}), " +
                                "(q:Question {QuestionId: $questionId}) " +
                                "CREATE (u)-[:POSTS_QUESTION]->(q); ",
                        parameters("userId", post.getOwnerUserId(), "questionId", post.getPostId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (q:Question {QuestionId: $questionId}) " +
                                "UNWIND $tagList as tagName " +
                                "MERGE (t:Tag {name: tagName}) " +
                                "CREATE (q)-[:CONTAINS_TAG]->(t);",
                        parameters("questionId", post.getPostId() ,"tagList", post.getTags()));
                return null;
            });
        }
    }

    //funzione che effettua la query per inserire la relazione Votes tra Answer e Post
    public void insertRelationVote(String userId, String answerId, int voteAnswer){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {userId: $userId}), " +
                                "(a:Answer {answerId: $answerId}) " +
                                "MERGE (u)-[r:VOTE]->(a) " +
                                "ON CREATE " +
                                "SET r.VoteTypeId = $voteAnswer " +
                                "ON MATCH " +
                                "SET r.VoteTypeId = $voteAnswer;",
                        parameters("userId", userId, "answerId", answerId, "voteAnswer", voteAnswer));
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
    public void removeAnswer(Answer answer, String postId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
               tx.run("MATCH (q: Question {QuestionId: $questionId})<-[ANSWERS_TO]-(a:Answer {answerId: $answerId}) " +
                       "DETACH DELETE a; ",
                       parameters("questionId", postId, "answerId", answer.getAnswerId()));
               return null;
            });
        }
    }

    //funzione che effettua la query per rimuovere la relazione Follows tra due utenti
    public void removeFollowRelationAndUpdate(String userIdFollower, String userIdFollowed){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (fr:User {userId: $userIdFollower})-[r:FOLLOW]->(fd:User {userId: $userIdFollowed}) " +
                                "DELETE r; ",
                        parameters("userIdFollower", userIdFollower, "userIdFollowed", userIdFollowed));
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
                tx.run("MATCH (a:Answer)-[:ANSWERS_TO]->(q:Question {QuestionId: $questionId}) " +
                                "DETACH DELETE a, q; ",
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

    public void removeRelationVote(String userId, String answerId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User {userId: $userId})-[r:VOTE]->(:Answer {answerId: $answerId}) " +
                                "DELETE r",
                        parameters("userId", userId, "answerId", answerId));
                return null;
            });
        }
    }

    public void removeUser(String userId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {userId: $userId})-[:FOLLOW]-(:User), " +
                "(u)-[:POSTS_QUESTION]->(q:Question)<-[:ANSWERS_WITH]-(a2:Answer), " +
                "(u)-[:ANSWERS_WITH]->(a:Answer), " +
                "DETACH DELETE u, q, a, a2; ",
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

    public int getVote(String userId, String answerId){
        try(Session session = dbConnection.session()){
            int voto = (int) session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User {userId: $userId})-[r:VOTE]->(:Answer {answerId: $answerId}) " +
                                "return r.VoteTypeId as Voto LIMIT 1",
                        parameters("userId", userId, "answerId", answerId));
                if (result.hasNext()) {
                    // esiste un voto
                    return result.single().get("Voto",0);
                }
                return 0;
            });
            return voto;
        }
    }

}
