package it.unipi.dii.server.databaseDriver;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import javafx.util.Pair;
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
        String password = "NEO4J";
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
                            "RETURN t.tagNames as Name, count(*) AS NQuestions " +
                            "ORDER BY NQuestions DESC " +
                            "LIMIT 10").list();
                }
            });

            for(final Record record : records){
                tags.put(record.get("Name").asString(), record.get("NQuestions").asInt());
            }
            return tags;
        }

    }
/*


 */
    public ArrayList<Answer> findUserAnswers(String username){
        try (Session session = dbConnection.session())
        {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                                        CALL db.index.fulltext.queryNodes("display_name_fulltext_index", $username)
                                        YIELD node
                                        MATCH (node)-[:ANSWERS_WITH]->(a:Answer)<-[v:VOTE]-(uv:User)
                                        RETURN a.answerId as answerId, a.body as body, sum(v.VoteTypeId) as score ORDER BY score DESC
                                        """,
                                        parameters( "username", username) );
                ArrayList<Answer> answers = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    answers.add(
                            new Answer()
                                .setBody(r.get("body").asString())
                                .setScore(r.get("score").asInt())
                                .setParentPostId(r.get("answerId").asString()));
                }
                return answers;
            });
        }
    }

    //si potrebbe aggiungere l'immagine del profilo (se inserita nel graph) alle cose da prendere
    //funzione che effettua la query per trovare gli utenti correlati all'utente username (amici di amici)
    public ArrayList<String> getCorrelatedUsers(String username){
        ArrayList<String> users = new ArrayList<>();

        try (Session session = dbConnection.session())
        {  //u2 might follow u back, so a cycle is present (u3 <> u), and u3 should not be already followed by u (so u3 <> u2)
            return session.readTransaction(tx -> {
                Result result = tx.run( "MATCH (u3:User)<-[:FOLLOW]-(u2:User)<-[:FOLLOW]-(u:User {displayName: $userId}) " +
                                "WHERE u3 <> u and u3 <> u2  " +
                                "RETURN distinct u3.displayName AS Username " +
                                "LIMIT 20; ",
                        parameters( "userId", username) );

                while(result.hasNext())
                {
                    Record r = result.next();
                    users.add(r.get("Username").asString());
                }
                return users;
            });
        }
    }

    //TODO: controllare questa query
    //funzione che effettua la query per trovare gli utenti correlati ad un certo tag
    public ArrayList<String> getRecommendedUsers(String userId, String tagName){
        try (Session session = dbConnection.session())
        {
            return session.readTransaction(tx -> {
                Result result = tx.run("MATCH (u2: User)-[:POSTS_QUESTION]->(:Question)-[:CONTAINS_TAG]->(t:Tag {tagNames: $name}) "+
                                "WHERE u2.userId <> $userId " +
                                "RETURN distinct u2.displayName as Username " +
                                "LIMIT 10;",
                        parameters("userId", userId, "name", tagName));
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


    //returns who follows the userId
    public ArrayList<String> getUserIdsFollower(String userId) {
        try(Session session = dbConnection.session()){
            return session.writeTransaction(tx -> {
                ArrayList<String> userIdsFollower = new ArrayList<>();
                tx.run("MATCH (fr:User)-[:FOLLOW]->(fd:User {displayName: $userIdFollowed}) " +
                                        "RETURN fr.displayName as userIdFollower ",
                                parameters("userIdFollowed", userId))
                        .stream().forEach(record ->
                                userIdsFollower.add(record.get("userIdFollower").asString())
                        );
                System.out.println("found " + userIdsFollower.size() + "followers");
                return userIdsFollower;
            });
        }
    }

    //returns who the userId follows
    public ArrayList<String> getUserIdsFollowed(String userId) {
        try(Session session = dbConnection.session()){
            return session.writeTransaction(tx -> {
                ArrayList<String> userIdsFollowed = new ArrayList<>();
                tx.run("MATCH (fr:User {displayName: $userIdFollower})-[:FOLLOW]->(fd:User) " +
                                        "RETURN fd.displayName as userIdFollowed ",
                                parameters("userIdFollower", userId))
                        .stream().forEach(record ->
                                userIdsFollowed.add(record.get("userIdFollowed").asString())
                        );

                System.out.println("found " + userIdsFollowed.size() + "followers");
                return userIdsFollowed;
            });
        }
    }

    //funzione che effettua la query per per inserire il nodo Answer
    public void insertAnswer(Answer answer){
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
                        parameters("answerId", answer.getAnswerId(), "questionId", answer.getParentPostId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {userId: $DisplayName}), " +
                                "(a:Answer {answerId: $answerId}) " +
                                "CREATE (u)-[:POSTS_ANSWER]->(a); ",
                        parameters("DisplayName", answer.getOwnerUserName(), "answerId", answer.getAnswerId()));
                return null;
            });
        }

    }

    //funzione che effettua la query per inserire la relazione Follow tra due username
    public void insertFollowRelationAndUpdate(String userIdFollower, String userIdFollowed){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (fd:User {userId: $userIdFollowed}), " +
                                "(fr:User {userId: $userIdFollower}) " +
                                "CREATE (fr)-[:FOLLOWS]->(fd); ",
                        parameters("userIdFollowed", userIdFollowed, "userIdFollower", userIdFollower));
                return null;
            });
        }
    }

    //funzione che effettua la query per inserire il nodo Post
    public void insertPost(Post post){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx ->{
                tx.run("CREATE (q:Question {questionId: $questionId}); ",
                        parameters("questionId", post.getGlobalId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {userId: $userId}), " +
                                "(q:Question {questionId: $questionId}) " +
                                "CREATE (u)-[:POSTS_QUESTION]->(q); ",
                        parameters("userId", post.getOwnerUserId(), "questionId", post.getGlobalId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (q:Question {questionId: $questionId}) " +
                                "FOREACH (tagName IN $tagList | MERGE (q)-[:CONTAINS_TAG]->(t:Tag {name: tagName})); ",
                        parameters("questionId", post.getMongoPost_id() ,"tagList", post.getTags()));
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
                tx.run("MATCH (a:Answer {answerId: $answerId}) " +
                                "DETACH DELETE a; ",
                        parameters("answerId", answer.getAnswerId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:Answer {answerId: $answerId})-[r:ANSWERS_TO]->(:Question {questionId: $questionId}) " +
                                "DELETE r; ",
                        parameters( "questionId", postId, "answerId", answer.getAnswerId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User {userId: $userId})-[r:POSTS_ANSWER]->(:Answer {answerId: $answerId}) " +
                                "DELETE r; ",
                        parameters("userId", answer.getOwnerUserId(), "answerId", answer.getAnswerId()));
                return null;
            });
        }
    }

    //funzione che effettua la query per rimuovere la relazione Follows tra due utenti
    public void removeFollowRelationAndUpdate(String userIdFollower, String userIdFollowed){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (fr:User {userId: $userIdFollower})-[r:FOLLOWS]->(fd:User {userId: $userIdFollowed}) " +
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
                tx.run("MATCH (a:Answer)-[:ANSWERS_TO]->(:Question {questionId: $questionId}) " +
                                "DETACH DELETE a; ",
                        parameters("questionId", post.getGlobalId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (q:Question {questionId: $questionId}) " +
                                "DETACH DELETE q; ",
                        parameters("questionId", post.getGlobalId()));
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
                tx.run("MATCH (:User {userId: $userId})-[r:VOTE]->(:Answer {answerId: $answerId})" +
                                "DELETE r",
                        parameters("userId", userId, "answerId", answerId));
                return null;
            });
        }
    }

    public void removeUser(String userName){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {displayName: $username}) DELETE u;",
                        parameters("userId", userName));
                return null;
            });
        }
    }

    public int getVote(String userId, String answerId){
        try(Session session = dbConnection.session()){
            int voto = (int) session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User {userId: $userId})-[r:VOTE]->(:Answer {answerId: $answerId})" +
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

    /*
    The following query outputs data in this format
                                                              NUMBER
                    NUMBER                                OF QUESTIONS USER
                      OF                                ANSWERED CONTAINING THE
      USERNAME     FOLLOWERS                              CORRESPONDING TAG
    ╒═══════════╤══════════════╤════════════════════════╤════════════════╕
    │"top_users"│"folllower_no"│"tag_names"             │"tags_top_users"│
    ╞═══════════╪══════════════╪════════════════════════╪════════════════╡
    │"Brian"    │948           │"python"                │112             │
    ├───────────┼──────────────┼────────────────────────┼────────────────┤
    │"Rob"      │648           │"c++"                   │69              │
    ├───────────┼──────────────┼────────────────────────┼────────────────┤
    │"Brian"    │948           │"c#"                    │54              │
    ╘════════════════════════════════════════════════════════════════════╛

    I decided to use a Post object as the key because it encapsulate both username and follower number.
    Each user has a lists of Pair containing a post object whose only significant attribute is the tag list, namely
    a list with only one element(the one in the "tag_names" column above) and an integer corresponding  to the "tags_top_users"
    field.
   */

    public HashMap<User, ArrayList<Pair<Post, Integer>>> findHotTopicsForTopUsers(){
        String query =
                """
                    MATCH (topUsers:User)<-[f:FOLLOW]-(otherUsers:User)
                    WITH topUsers.displayName as t_us, count(*) as follower_no
                    ORDER BY follower_no DESC LIMIT 10
                    MATCH (t:Tag)<-[c_tag:CONTAINS_TAG]-(q:Question)<-[b_to:BELONGS_TO] -(a:Answer)
                                <-[an_with:ANSWERS_WITH]-(u:User{displayName:t_us})
                    WITH u.displayName as top_users, follower_no, t.tagNames as tag_names, count(*) as tags_top_users
                    ORDER BY tags_top_users  DESC
                    RETURN top_users, follower_no, tag_names, tags_top_users
                    LIMIT 10
                """;

        try(Session session = dbConnection.session()){
            return  session.readTransaction(tx -> {
                HashMap<User, ArrayList<Pair<Post, Integer>>> hotTopicsForTopUsersHashMap = new HashMap<>();
                Result result = tx.run(query);
                while (result.hasNext()) {
                    Record record = result.next();
                    User u = new User()
                            .setFollowersNumber(record.get("follower_no").asInt())
                            .setDisplayName(record.get("top_users").asString());
                    ArrayList<String> l = new ArrayList<>();
                    l.add(record.get("tag_names").asString());
                    Post p = new Post().setTags(l);
                    if(!hotTopicsForTopUsersHashMap.containsKey(u)){
                        hotTopicsForTopUsersHashMap.put(u, new ArrayList<Pair<Post, Integer>>());
                    }

                    ((ArrayList<Pair<Post, Integer>>)hotTopicsForTopUsersHashMap.get(u)).add(
                            new Pair(p, record.get("tags_top_users").asInt()));


                }
                return hotTopicsForTopUsersHashMap;
            });
        }
    }

     /*
    The following query outputs data in this format

    ╒═══════════╤═══════════════════════════════════════════════════════════════════════════════════════╤════════════╕
    │"top_users"│"title"                                                                                │"answers_no"│
    ╞═══════════╪═══════════════════════════════════════════════════════════════════════════════════════╪════════════╡
    │"Matt"     │"What is your favourite MATLAB/Octave programming trick?"                              │21          │
    ├───────────┼───────────────────────────────────────────────────────────────────────────────────────┼────────────┤
    │"Matt"     │"Is there a difference between Select * and Select [list each col]"                    │18          │
    ├───────────┼───────────────────────────────────────────────────────────────────────────────────────┼────────────┤
    │"Matt"     │"Can you search SQL Server 2005 Stored Procedure content?"                             │13          │
    ├───────────┼───────────────────────────────────────────────────────────────────────────────────────┼────────────┤
    │"Brian"    │"What exactly do you do when your team leader is incompetent?"                         │19          │
    ├───────────┼───────────────────────────────────────────────────────────────────────────────────────┼────────────┤
    │"Brian"    │"Continue Considered Harmful?"                                                         │16          │
    ├───────────┼───────────────────────────────────────────────────────────────────────────────────────┼────────────┤
    │"Brian"    │"What features do you wish were in common languages?"                                  │14          │
    ╘════════════════════════════════════════════════════════════════════════════════════════════════════════════════╛

   */


    /*
        MATCH (topUsers:User)<-[f:FOLLOW]-(otherUsers:User)
        WITH topUsers.displayName as top_users, count(*) as folllower_no
        ORDER BY folllower_no DESC LIMIT 10
        CALL  {
            WITH top_users
            MATCH (a:Answer)-[b_to:BELONGS_TO] ->(quest:Question)<-[pq:POSTS_QUESTION]-(u:User{displayName:top_users})
            WITH u.displayName as t_users, quest.Title as title,  count(*) as answers_no
            ORDER BY u.displayName, answers_no DESC LIMIT 3
            RETURN title, answers_no
        }
        RETURN top_users, title, answers_no
     */
    public Map<User, ArrayList<Post>> findMostAnsweredTopUserPosts() {
        String query =
                """
                MATCH (topUsers:User)<-[f:FOLLOW]-(otherUsers:User)
                WITH topUsers.displayName as top_users, count(*) as folllower_no
                ORDER BY folllower_no DESC LIMIT 10
                CALL {
                    WITH top_users
                    MATCH (a:Answer)-[b_to:BELONGS_TO]->(quest:Question)<-[pq:POSTS_QUESTION]-(u:User{displayName:top_users})
                    WITH u.displayName as t_users, quest.Title as title, count(*) as answers_no
                    ORDER BY u.displayName, answers_no DESC LIMIT 3
                    RETURN title, answers_no
                }
                RETURN top_users, title, answers_no        
                """;

        try(Session session = dbConnection.session()){
            return  session.readTransaction(tx -> {
                HashMap<User, ArrayList<Post>> mostAnsweredTopUserPostsHashMap = new HashMap<>();
                Result result = tx.run(query);
                while (result.hasNext()) {
                    User u = new User().setDisplayName(result.next().get("top_users").asString());
                    Post p = new Post()
                            .setTitle(result.next().get("title").asString())
                            .setOwnerUserName(result.next().get("top_users").asString());
                    if(!mostAnsweredTopUserPostsHashMap.containsKey(u)){
                        mostAnsweredTopUserPostsHashMap.put(u, new ArrayList<Post>());
                    }
                    ((ArrayList<Post>)mostAnsweredTopUserPostsHashMap.get(u)).add(p);
                }
                return mostAnsweredTopUserPostsHashMap;
            });
        }
    }

}