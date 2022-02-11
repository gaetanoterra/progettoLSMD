package it.unipi.dii.server.databaseDriver;

import com.mongodb.client.MongoClients;
import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import javafx.util.Pair;
import org.neo4j.driver.*;
import java.util.*;

import static org.neo4j.driver.Values.NULL;
import static org.neo4j.driver.Values.parameters;

import org.neo4j.driver.Record;


//classe preposta ad effettuare le query del graph database
public class GraphDBManager {

    private final Driver dbConnection;

    private void init() {
        //
        try (Session session = dbConnection.session()){
            session.run("CREATE FULLTEXT INDEX displayname_fulltext_index IF NOT EXISTS FOR (n:User) ON EACH [ n.displayName]");
        }
    }

    public GraphDBManager(DBExecutionMode dbe) {
        String uri = null;
        switch (dbe) {
            case LOCAL -> uri = "bolt://localhost:7687";
            case CLUSTER -> uri = "bolt://172.16.4.117:7687";
        }
        String user = "neo4j";
        String password = "NEO4J";
        dbConnection = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        init();
    }

    public GraphDBManager(){
        this(DBExecutionMode.LOCAL);
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

    public ArrayList<Post> getPostByOwnerUsername(String ownerPostUsername) {
        try(Session session = dbConnection.session()){
            return session.writeTransaction(tx -> {
                ArrayList<Post> userPosts = new ArrayList<>();
                tx.run("MATCH (u:User {displayName: $userDisplayName})-[:POSTS_QUESTION]->(q:Question) " +
                                        "RETURN q.QuestionId as questionId, q.Title as title",
                                parameters("userDisplayName", ownerPostUsername))
                        .stream().forEach(record ->
                                userPosts.add(
                                        new Post(null,
                                                record.get("questionId").asString(),
                                                record.get("title").asString(),
                                                null,
                                                null,
                                                null,
                                                null,
                                                ownerPostUsername,
                                                null))
                        );

                System.out.println("found " + userPosts.size() + " posts");
                return userPosts;
            });
        }

    }


    //CREATE FULLTEXT INDEX displayname_fulltext_index IF NOT EXISTS FOR (n:User) ON EACH [ n.displayName]
    public ArrayList<Answer> findUserAnswers(String username){
        try (Session session = dbConnection.session())
        {
            return session.readTransaction(tx -> {
                Result result = tx.run("""
                                        CALL db.index.fulltext.queryNodes("displayname_fulltext_index", $username)
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

                System.out.println("found " + answers.size() + " answers");

                return answers;
            });
        }
    }

    //si potrebbe aggiungere l'immagine del profilo (se inserita nel graph) alle cose da prendere
    //funzione che effettua la query per trovare gli utenti correlati all'utente username (amici di amici)
    public ArrayList<User> getCorrelatedUsers(String username){
        ArrayList<User> users = new ArrayList<>();

        try (Session session = dbConnection.session())
        {  //u2 might follow u back, so a cycle is present (u3 <> u), and u3 should not be already followed by u (so u3 <> u2)
            return session.readTransaction(tx -> {
                Result result = tx.run( "MATCH (u3:User)<-[:FOLLOW]-(u2:User)<-[:FOLLOW]-(u:User {displayName: $displayName}) " +
                                "WHERE u3 <> u and u3 <> u2  " +
                                "RETURN distinct u3.displayName AS Username, u3.profileImage as profileImage " +
                                "LIMIT 20; ",
                        parameters( "displayName", username) );

                while(result.hasNext())
                {
                    Record r = result.next();
                    users.add(new User(
                            null,
                            r.get("Username").asString(),
                            null,
                            null,
                            null,
                            null,
                            r.get("profileImage").asString()));
                }
                return users;
            });
        }
    }

    //TODO: controllare questa query
    //funzione che effettua la query per trovare gli utenti correlati ad un certo tag
    public ArrayList<User> getRecommendedUsers(String displayName, String tagName){
        try (Session session = dbConnection.session())
        {
            return session.readTransaction(tx -> {
                Result result = tx.run("MATCH (u2: User)-[:POSTS_QUESTION]->(:Question)-[:CONTAINS_TAG]->(t:Tag {tagNames: $name}) "+
                                "WHERE u2.displayName <> $displayName " +
                                "RETURN distinct u2.displayName as Username, u2.profileImage as profileImage " +
                                "LIMIT 10;",
                        parameters("displayName", displayName, "name", tagName));
                ArrayList<User> users = new ArrayList<>();
                while(result.hasNext())
                {
                    Record r = result.next();
                    String image;

                    if(r.get("profileImage") == NULL)
                        image = "";
                    else
                        image = r.get("profileImage").asString();

                    users.add(new User(
                            null,
                            r.get("Username").asString(),
                            null,
                            null,
                            null,
                            null,
                            image));
                }
                return users;
            });
        }
    }


    //returns who follows the userId
    public ArrayList<User> getUserIdsFollower(String displayName) {
        try(Session session = dbConnection.session()){
            return session.writeTransaction(tx -> {
                ArrayList<User> userFollower = new ArrayList<>();
                tx.run("MATCH (fr:User)-[:FOLLOW]->(fd:User {displayName: $displayNameFollowed}) " +
                                        "RETURN fr.displayName as userDisplayNameFollower, fr.profileImage as profileImage ",
                                parameters("displayNameFollowed", displayName))
                        .stream().forEach(record ->
                                userFollower.add(
                                        new User(null,
                                                record.get("userDisplayNameFollower").asString(),
                                                null,
                                                null,
                                                null,
                                                null,
                                                record.get("profileImage").asString()))
                        );

                System.out.println("found " + userFollower.size() + "followers");
                return userFollower;
            });
        }
    }

    //returns who the userId follows
    public ArrayList<User> getUserIdsFollowed(String displayName) {
        try(Session session = dbConnection.session()){
            return session.writeTransaction(tx -> {
                ArrayList<User> userFollowed = new ArrayList<>();
                tx.run("MATCH (fr:User {displayName: $displayNameFollower})-[:FOLLOW]->(fd:User) " +
                                        "RETURN fd.displayName as userDisplayNameFollowed, fd.profileImage as profileImage ",
                                parameters("displayNameFollower", displayName))
                        .stream().forEach(record ->
                                userFollowed.add(new User(
                                        null,
                                        record.get("userDisplayNameFollowed").asString(),
                                        null,
                                        null,
                                        null,
                                        null,
                                        record.get("profileImage").asString()))
                        );

                System.out.println("found " + userFollowed.size() + "followers");
                return userFollowed;
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
                                "(q:Question {QuestionId: $questionId}) " +
                                "CREATE (a)-[:BELONGS_TO]->(q); ",
                        parameters("answerId", answer.getAnswerId(), "questionId", answer.getParentPostId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {displayName: $DisplayName}), " +
                                "(a:Answer {answerId: $answerId}) " +
                                "CREATE (u)-[:ANSWERS_WITH]->(a); ",
                        parameters("DisplayName", answer.getOwnerUserName(), "answerId", answer.getAnswerId()));
                return null;
            });
        }

    }

    //funzione che effettua la query per inserire la relazione Follow tra due username
    public void insertFollowRelationAndUpdate(String userDisplayNameFollower, String userDisplayNameFollowed){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (fd:User {displayName: $userDisplayNameFollowed}), " +
                                "(fr:User {displayName: $userDisplayNameFollower}) " +
                                "CREATE (fr)-[:FOLLOW]->(fd); ",
                        parameters("userDisplayNameFollowed", userDisplayNameFollowed, "userDisplayNameFollower", userDisplayNameFollower));
                return null;
            });
        }
    }

    //funzione che effettua la query per inserire il nodo Post
    public void insertPost(Post post){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx ->{
                tx.run("CREATE (q:Question {QuestionId: $questionId, Title: $title}); ",
                        parameters("questionId", post.getGlobalId(), "title", post.getTitle()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {displayName: $displayName}), " +
                                "(q:Question {QuestionId: $questionId}) " +
                                "CREATE (u)-[:POSTS_QUESTION]->(q); ",
                        parameters("displayName", post.getOwnerUserName(), "questionId", post.getGlobalId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (q:Question {QuestionId: $questionId}) " +
                                "FOREACH (tagName IN $tagList | MERGE (q)-[:CONTAINS_TAG]->(t:Tag {tagNames: tagName})); ",
                        parameters("questionId", post.getGlobalId() ,"tagList", post.getTags()));
                return null;
            });
        }
    }

    //funzione che effettua la query per inserire la relazione Votes tra Answer e Post
    public void insertRelationVote(String displayName, String answerId, int voteAnswer){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {displayName: $displayName}), " +
                                "(a:Answer {answerId: $answerId}) " +
                                "MERGE (u)-[r:VOTE]->(a) " +
                                "ON CREATE " +
                                "SET r.VoteTypeId = $voteAnswer " +
                                "ON MATCH " +
                                "SET r.VoteTypeId = $voteAnswer;",
                        parameters("displayName", displayName, "answerId", answerId, "voteAnswer", voteAnswer));
                return null;
            });
        }
    }

    //funzione che effettua la query per inserire il nodo User
    public void insertUser(User user){
        try (Session session = dbConnection.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "CREATE (u:User {displayName: $displayName}); ",
                        parameters( "displayName", user.getDisplayName() ) );
                return null;
            });
        }
    }

    //funzione che effettua la query per rimuovere il nodo Answer
    public void removeAnswer(Answer answer){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:Answer {answerId: $answerId}) " +
                                "DETACH DELETE a; ",
                        parameters("answerId", answer.getAnswerId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:Answer {answerId: $answerId})-[r:BELONGS_TO]->(:Question {QuestionId: $questionId}) " +
                                "DELETE r; ",
                        parameters( "questionId", answer.getParentPostId(), "answerId", answer.getAnswerId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User {displayName: $displayName})-[r:ANSWERS_WITH]->(:Answer {answerId: $answerId}) " +
                                "DELETE r; ",
                        parameters("displayName", answer.getOwnerUserName(), "answerId", answer.getAnswerId()));
                return null;
            });
        }
    }

    //funzione che effettua la query per rimuovere la relazione Follows tra due utenti
    public void removeFollowRelationAndUpdate(String userDisplayNameFollower, String userdisplayNameFollowed){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (fr:User {displayName: $userDisplayNameFollower})-[r:FOLLOW]->(fd:User {displayName: $userdisplayNameFollowed}) " +
                                "DELETE r; ",
                        parameters("userDisplayNameFollower", userDisplayNameFollower, "userdisplayNameFollowed", userdisplayNameFollowed));
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
                tx.run("MATCH (a:Answer)-[:BELONGS_TO]->(:Question {QuestionId: $questionId}) " +
                                "DETACH DELETE a; ",
                        parameters("questionId", post.getGlobalId()));
                return null;
            });
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (q:Question {QuestionId: $questionId}) " +
                                "DETACH DELETE q; ",
                        parameters("questionId", post.getGlobalId()));
                return null;
            });
            /*
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (t:Tag) " +
                                "WHERE t.name in $tagList AND WHERE NOT (t)<-[:CONTAINS_TAG]-() " +
                                "DELETE t; ",
                        parameters("tagList", post.getTags()));
                return null;
            });*/
        }
    }

    public void removeRelationVote(String displayName, String answerId){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (:User {displayName: $displayName})-[r:VOTE]->(:Answer {answerId: $answerId})" +
                                "DELETE r",
                        parameters("displayName", displayName, "answerId", answerId));
                return null;
            });
        }
    }

    public void removeUser(String displayName){
        try(Session session = dbConnection.session()){
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (u:User {displayName: $displayName}) DETACH DELETE u;",
                        parameters("displayName", displayName));
                return null;
            });
        }
    }

    public int getVote(String displayName, String answerId){
        try(Session session = dbConnection.session()){
            int voto = (int) session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User {displayName: $displayName})-[r:VOTE]->(:Answer {answerId: $answerId})" +
                                "return r.VoteTypeId as Voto LIMIT 1",
                        parameters("displayName", displayName, "answerId", answerId));
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

    //TODO: questa non viene chiamata da nessuna parte, infilarla
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

    public boolean checkFollowRelation(String displayName, String displayNameToCheck) {
        try(Session session = dbConnection.session()){
            boolean exists = (boolean) session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User {displayName: $displayName})-[r:FOLLOW]->(:User {displayName: $displayNameToCheck})" +
                                "return r LIMIT 1",
                        parameters("displayName", displayName, "displayNameToCheck", displayNameToCheck));
                if (result.hasNext()) {
                    // esiste il follow
                    return true;
                }
                return false;
            });
            return exists;
        }
    }
}