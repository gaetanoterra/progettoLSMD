package it.unipi.dii.server.databaseDriver;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import com.mongodb.client.model.Filters;
import javafx.util.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.BasicDBObject;

//TODO: Necessaria revisione dei metodi per verificare se sono stati implementati nella loro completezza
public class DocumentDBManager {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public DocumentDBManager(){
        this(DBExecutionMode.LOCAL);
    }

    public DocumentDBManager(DBExecutionMode dbe){
        switch (dbe) {
            case LOCAL   -> mongoClient = MongoClients.create("mongodb://localhost:27017");
            case REMOTE  -> mongoClient = MongoClients.create("mongodb://localhost:27017");   //TODO: aggiungere Modalità remota
            case CLUSTER -> mongoClient = MongoClients.create("mongodb://host-1:27020, host-2:27020, host-3:27020/?retryWrites=true&w=majority&wtimeout=10000");
        }
        mongoDatabase = mongoClient.getDatabase("PseudoStackOverDB");
    }

    public void close(){
        this.mongoClient.close();
    }

    public Map<User, Post[]> findMostAnsweredTopUserPosts(){
/* QUESTA VA FATTA CON NEO4J
        //Find 50 most followed users, and for each of them show the 3 posts they wrote that contains the largest number of answers

        MongoCollection<Document> collPost = mongoDatabase.getCollection("Post");
        MongoCollection<Document> collUser = mongoDatabase.getCollection("User");
        final int MAX_NUMBER_USERS = 50;
        final int MAX_NUMBER_POSTS = 3;

        ArrayList<String> userIdList = new ArrayList<>();
        Bson projectStage = project(
                fields(
                        include(
                                "$Id",
                                "$followerNumber"
                        )
                )
        );
        Bson sortStage = sort(descending("followedNumber"));
        Bson limitStage = limit(MAX_NUMBER_USERS);
        collUser.aggregate(
                Arrays.asList(
                        projectStage,
                        sortStage,
                        limitStage
                )
        ).forEach(doc ->
                userIdList.add(doc.getString("Id"))
        );

        // adesso devo trovare i loro post
        Map<User, Post[]> utentiSeguitiPost = new HashMap<>();

        Bson matchStage = match(in("OwnerUserId", userIdList));
        Bson projectStage2 = new Document(
                "$project",
                new Document(
                        "OwnerUserId", 1
                ).append(
                        "PostId", 1
                ).append(
                        "NumeroRisposte",
                        new Document(
                                "$size", "$Answers"
                        )
                )
        );
        Bson groupByUserId = group(
                "$OwnerUserId",
                push("listaPostId","$PostId"),
                push("listaNumeroRisposte","$NumeroRisposte")
        );
        collPost.aggregate(
                Arrays.asList(
                        matchStage,
                        projectStage2,
                        groupByUserId
                )
        ).forEach(document -> {
            String ownerUserId = document.getString("ownerUserId");
            //listaPostId e listaNumeroRisposte hanno stessa dimensione
            ArrayList<String> listaPostId = (ArrayList<String>)document.get("listaPostId");
            ArrayList<Integer> listaNumeroRisposte = (ArrayList<Integer>)document.get("listaNumeroRisposte");
            ArrayList<Post> finalResult = new ArrayList<>();
            final int sizeArray = Math.min(listaNumeroRisposte.size(), MAX_NUMBER_POSTS);
            // trova i sizeArray valori più alti
            for (int i = 0; i < sizeArray; ++i) {
                Integer maxNumeroRisposte = listaNumeroRisposte
                        .stream()
                        .max(Comparator.naturalOrder())
                        .get();
                int indexToRemove = listaNumeroRisposte.indexOf(maxNumeroRisposte);
                String postId = listaPostId.get(indexToRemove);
                listaNumeroRisposte.remove(indexToRemove);
                listaPostId.remove(indexToRemove);
                finalResult.add(getPostById(postId));
            }
            utentiSeguitiPost.put(getUserById(ownerUserId), (Post[])finalResult.toArray());
        });
        return utentiSeguitiPost;
        */
        return null;
    }

    public String[] findMostPopularTagsByLocation(String location, int numTags){
        //trovo tutti gli utenti relativi ad una locazione
        //scorro tutti i post che hanno ownerUserId tra gli utenti trovati prima
        //raggruppo per tag e li conto
        MongoCollection<Document> collPost = mongoDatabase.getCollection("Post");
        MongoCollection<Document> collUser = mongoDatabase.getCollection("User");
        ArrayList<String> tagList = new ArrayList<>();
        ArrayList<String> userIdList = new ArrayList<>();

        collUser.find(eq("Location", location)).forEach(document -> {
            userIdList.add(document.getString("Id"));
        });

        /*try (MongoCursor<Document> cursor = collUser.find(eq("Location", location)).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();
                User u = new User();
                //mi interessa solo lo userId
                u.setId(doc.getString("Id"));
                userList.add(u);
            }
        }*/

        //adesso che ho la lista di utenti scorro i post e trovo quelli che hanno ownerUserId tra i miei
        Bson matchStage = match(in("OwnerUserId", (String[])userIdList.toArray()));
        Bson unwindStage = unwind("Tags");
        //raggruppando su un attributo, questo dovrebbe diventare _id, e perde il nome originale
        Bson groupStage = group("$Tags", sum("totaleTags",1));
        Bson sortStage = sort(descending("totaleTags"));
        Bson limitStage = limit(numTags);

        collPost.aggregate(
                Arrays.asList(
                        matchStage,
                        unwindStage,
                        groupStage,
                        sortStage,
                        limitStage
                )
        ).forEach(doc ->
                tagList.add(doc.getString("_id"))
        );

        /*try (MongoCursor<Document> cursor = collPost.aggregate(Arrays.asList(m, u, g, s, l)).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();
                tagList.add(doc.getString("tagList"));
            }
        }*/

        return (String[]) ((tagList.toArray().length == 0)? null : tagList.toArray());
    }

    //restituisco gli id degli utenti più esperti
    public User[] findTopExpertsByTag(String tag, int num){
        MongoCollection<Document> collPost = mongoDatabase.getCollection("Post");
        MongoCollection<Document> collUser = mongoDatabase.getCollection("User");
        ArrayList<String> userIdList = new ArrayList<>();
        ArrayList<User> userList = new ArrayList<>();

        Bson matchTag = match(eq("Tags", tag));
        Bson unwindAnswers = unwind("Answers");
        //raggruppando su un attributo, questo dovrebbe diventare _id, e perde il nome originale
        Bson groupByOwnerUserId = group("$Answers.OwnerUserId", sum("totaleRisposteUtente",1));
        Bson sortByCountDesc = sort(descending("totaleRisposteUtente"));
        Bson limitStage = limit(num);

        //Bson projectStage = project(fields(include("$Answers.OwnerUserId")));

        collPost.aggregate(
                Arrays.asList(
                        matchTag,
                        unwindAnswers,
                        groupByOwnerUserId,
                        sortByCountDesc,
                        limitStage
                )
        ).forEach(doc ->
                userIdList.add(doc.getString("_id"))
        );

        collUser.find(in("Id", (String[])userIdList.toArray())).forEach(document -> {
            User user = new User()
                    .setUserId(document.getString("Id"))
                    .setDisplayName(document.getString("DisplayName"))
                    .setPassword(document.getString("Password"))
                    .setFollowersNumber(document.getInteger("followerNumber"))
                    .setFollowedNumber(document.getInteger("followedNumber"))
                    .setReputation(document.getDouble("Reputation"))
                    .setCreationDate(document.getDate("CreationDate"))
                    .setLastAccessDate(document.getDate("LastAccessDate"))
                    .setType(document.getString("type"))
                    .setLocation(document.getString("Location"))
                    .setAboutMe(document.getString("AboutMe"))
                    .setWebsiteURL(document.getString("WebsiteURL"));
            userList.add(user);
        });

        return (User[]) ((userList.toArray().length == 0)? null : userList.toArray());
    }

    //TODO: questa è una query analytics, quindi definire un messaggio e un opcode
    public Map<User, Pair<String,Integer>[]> findHotTopicsForTopUsers(){
        // non riesco a ricordare quale era il metodo per completare questa operazione, quindi lo lascio qui
        /*
        3)Trova i 50 utenti più seguiti,
        e per ogni utente mostra
        i 3 tag per cui hanno scritto più risposte
        (a scopo di trovare gli hooooooooooot topics, tag più popolari nella top 3 degli utenti)
         */
        MongoCollection<Document> collPost = mongoDatabase.getCollection("Post");
        MongoCollection<Document> collUser = mongoDatabase.getCollection("User");
        HashMap<User, Pair<String,Integer>[]> result = new HashMap<>();
        /*
        db.users.aggregate([
            {$sort: {FollowersNumber: -1}},
            {$limit: 50}
        ])
        //50 at most users, the most followed ones
        */
        Bson sortByFollowersDesc = sort(descending("followerNumber"));
        Bson limitUsers = limit(50);
        collUser.aggregate(
                Arrays.asList(
                        sortByFollowersDesc,
                        limitUsers
                )
        ).forEach(document -> {
            User user = new User()
                    .setUserId(document.getString("Id"))
                    .setDisplayName(document.getString("DisplayName"))
                    .setPassword(document.getString("Password"))
                    .setFollowersNumber(document.getInteger("followerNumber"))
                    .setFollowedNumber(document.getInteger("followedNumber"))
                    .setReputation(document.getDouble("Reputation"))
                    .setCreationDate(document.getDate("CreationDate"))
                    .setLastAccessDate(document.getDate("LastAccessDate"))
                    .setType(document.getString("type"))
                    .setLocation(document.getString("Location"))
                    .setAboutMe(document.getString("AboutMe"))
                    .setWebsiteURL(document.getString("WebsiteURL"));
            // user done, now the three posts
            // for each user id ($Id)
            int userId = Integer.parseInt(user.getUserId());
            Bson matchOwnerUserId = match(eq("Answers.OwnerUserId", userId)); //ownerUserId è di tipo String
            Bson unwindAnswers = unwind("$Answers");
            Bson unwindTags = unwind("$Tags");
            Bson groupByTag = new Document("$group",
                    new Document("_id", "$Tags").append("count",
                            new Document("$sum", 1)));
            Bson sortByCountDesc = sort(descending("count"));
            Bson limitTags = limit(3);
            Bson projectTagCount = project(fields(Projections.computed("tag","_id"), include("count")));
                /*
                db.posts.aggregate([
                        {$match:
                {'answers.ownerUserId' : $Id}
            },
                {$unwind: "$Answers"},
                {$unwind: "$Tags"},
                {$group:
                {
                    _id: "$Tags",
                            count: {$sum: 1}
                }
                },
                {$sort: {count: -1}},
                {$limit: 3},
                {$project:
                {
                    tag: "$_id"
                    count: "$count"
                }
                }
        ])
         */
            ArrayList<Pair<String, Integer>> list = new ArrayList<>();
            collPost.aggregate(Arrays.asList(matchOwnerUserId, unwindAnswers, unwindTags, groupByTag, sortByCountDesc, limitTags, projectTagCount)).forEach(doc ->
                    list.add(new Pair<>(doc.getString("tag"), doc.getInteger("count")))
            );
            result.put(user, (Pair<String, Integer>[]) list.toArray());
        });
        return result;
    }

    public ArrayList<Post> getPostByDate(String data) {
        MongoCollection<Document> coll = mongoDatabase.getCollection("Post");

        ArrayList<Post> posts = new ArrayList<>();
        coll.find(eq("CreationDate", data)).forEach(doc -> {
            Post p = new Post(doc.getString("PostId"),
                    doc.getString("Title"),
                    (ArrayList<Answer>)doc.get("Answers"),
                    doc.getDate("CreationDate"),
                    doc.getString("Body"),
                    doc.getString("OwnerUserId"),
                    (ArrayList<String>)doc.get("Tags"));
            posts.add(p);
        });
        System.out.println("found " + posts.size() + " posts matching the date given as input");
        return posts;
    }

    public Post getPostById(String postId){
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("Post");

        Document postDoc = (Document) mongoCollection.find(eq("PostId", postId));
        ArrayList<Answer> answersList = new ArrayList<>();

        //    public Answer(String answerId, String creationDate, double score, String ownerUserName, String body) {
        postDoc.getList("Answers", Document.class).forEach((answer) -> {
            answersList.add(
                    new Answer(
                            answer.getString("Id"),
                            answer.getDate("CreationDate" ),
                            answer.getDouble("ViewCount"),
                            answer.getString("OwnerDisplayName"),
                            answer.getString("Body")
                    )
            );
        });

        postDoc.getList("Tags", Document.class).forEach((answer) -> {
            answersList.add(
                    new Answer(
                            answer.getString("Id"),
                            answer.getDate("CreationDate" ),
                            answer.getDouble("ViewCount"),
                            answer.getString("OwnerDisplayName"),
                            answer.getString("Body")
                    )
            );
        });
        return new Post(postId,
                        postDoc.getString("Title"),
                        answersList,
                        postDoc.getDate("CreationDate"),
                        postDoc.getString("Body"),
                        postDoc.getString("OwnerDisplayName"),
                        postDoc.getList("Tags", String.class)
        );

    }

    public ArrayList<Post> getPostByOwnerUsername(String username) {
        MongoCollection<Document> coll = mongoDatabase.getCollection("Post");

        ArrayList<Post> posts = new ArrayList<>();
        coll.find(all("OwnerUserId", username)).forEach(doc -> {
            Post p = new Post(doc.getString("PostId"),
                    doc.getString("Title"),
                    (ArrayList<Answer>)doc.get("Answers"),
                    doc.getDate("CreationDate"),
                    doc.getString("Body"),
                    doc.getString("OwnerUserId"),
                    (ArrayList<String>)doc.get("Tags"));

            posts.add(p);
        });

        System.out.println("found " + posts.size() + " posts matching the username given as input");
        return posts;
    }

    public ArrayList<Post> getPostsByTag(String[] tags){
        MongoCollection<Document> coll = mongoDatabase.getCollection("Post");

        ArrayList<Post> postArrayList = new ArrayList<>();
        coll.find(all("Tags", tags)).forEach(doc -> {
            Post p = new Post(doc.getString("PostId"),
                    doc.getString("Title"),
                    (ArrayList<Answer>)doc.get("Answers"),
                    doc.getDate("CreationDate"),
                    doc.getString("Body"),
                    doc.getString("OwnerUserId"),
                    (ArrayList<String>)doc.get("Tags"));

            postArrayList.add(p);
        });
        System.out.println("found " + postArrayList.size() + " posts matching the tag given as input");
        return postArrayList;
    }

    /*
        db.Posts.find(
            {
                $text: {$search: "??"},
                $or:[
                    {Title: {$regex: /.*??.* /i}},
                    {Body:{$regex: /.*??.* /i}}
                ]
            },
            {
                Title:1,
                Tags:1,
                numberOfAnswers: { $size: "$Answers" },
                Views:1
            }
        );
    */
    public ArrayList<Post> getPostsByText(String text){
        // controllo il titolo per semplicità (e velocità), si può cambiare ovviamente con il body
        MongoCollection<Document> collection = mongoDatabase.getCollection("Posts");
        ArrayList<Post> postArrayList = new ArrayList<>();
        collection.find(and(
                            Filters.text(text),
                            or(
                               regex("Title",".*" +text +".*","i"),
                               regex("Body" ,".*" +text +".*","i")
                            ))
                        )
                        .projection(new Document("Title",1)
                                    .append("Views", 1)
                                    .append("Title", 1)
                                    .append("Tags" , 1)
                                    .append("AnswersNumber",new BasicDBObject("$size","$Answers"))
                        )
                        .forEach(doc -> {
                            Post p = new Post(doc.getString("PostId"),
                                              doc.getString("Title"),
                                              doc.getInteger("AnswersNumber"),
                                              doc.getString("OwnerUserId"),
                                              doc.getList("Tags", String.class));
                            postArrayList.add(p);
                        }
        );

        System.out.println("found " + postArrayList.size() + " posts matching the text given as input");
        return postArrayList;
    }

    public User getUserById(String userId) {
        MongoCollection<Document> coll = mongoDatabase.getCollection("User");

        Document userDoc = coll.find(eq("Id", userId)).first();
        User user = new User();

        if(userDoc != null) {
            user.setUserId(userId)
                    .setDisplayName(userDoc.getString("DisplayName"))
                    .setPassword(userDoc.getString("Password"))
                    .setFollowersNumber(userDoc.getInteger("followerNumber"))
                    .setFollowedNumber(userDoc.getInteger("followedNumber"))
                    .setReputation(userDoc.getDouble("Reputation"))
                    .setCreationDate(userDoc.getDate("CreationDate"))
                    .setLastAccessDate(userDoc.getDate("LastAccessDate"))
                    .setType(userDoc.getString("type"))
                    .setLocation(userDoc.getString("Location"))
                    .setAboutMe(userDoc.getString("AboutMe"))
                    .setWebsiteURL(userDoc.getString("WebsiteURL"));
        }

        return user;
    }

    public User getUserData(String displayName){
        MongoCollection<Document> coll = mongoDatabase.getCollection("User");

        Document userDoc = coll.find(eq("DisplayName", displayName)).first();
        User user = new User();

        if(userDoc != null) {
            user.setUserId(userDoc.getString("Id"))
                    .setDisplayName(displayName)
                    .setPassword(userDoc.getString("Password"))
                    .setFollowersNumber(userDoc.getInteger("followerNumber"))
                    .setFollowedNumber(userDoc.getInteger("followedNumber"))
                    .setReputation(userDoc.getDouble("Reputation"))
                    .setCreationDate(userDoc.getDate("CreationDate"))
                    .setLastAccessDate(userDoc.getDate("LastAccessDate"))
                    .setType(userDoc.getString("type"))
                    .setLocation(userDoc.getString("Location"))
                    .setAboutMe(userDoc.getString("AboutMe"))
                    .setWebsiteURL(userDoc.getString("WebsiteURL"));
        }

        return user;
    }

    public User[] getUsersRank(){
        MongoCollection<Document> coll = mongoDatabase.getCollection("User");

        ArrayList<User> user = new ArrayList<>();
        coll.find().sort(descending("Reputation")).limit(10).forEach(doc -> {
            User u = new User()
                    .setUserId(doc.getString("Id"))
                    .setDisplayName(doc.getString("DisplayName"))
                    .setPassword(doc.getString("Password"))
                    .setFollowersNumber(doc.getInteger("followerNumber"))
                    .setFollowedNumber(doc.getInteger("followedNumber"))
                    .setReputation(doc.getDouble("Reputation"))
                    .setCreationDate(doc.getDate("CreationDate"))
                    .setLastAccessDate(doc.getDate("LastAccessDate"))
                    .setType(doc.getString("type"))
                    .setLocation(doc.getString("Location"))
                    .setAboutMe(doc.getString("AboutMe"))
                    .setWebsiteURL(doc.getString("WebsiteURL"));

            user.add(u);
        });

        return (User[]) user.toArray();
    }

    public boolean insertAnswer(Answer answer, String postId){
        MongoCollection<Document> coll = mongoDatabase.getCollection("Post");

        Document doc = new Document("AnswerId", answer.getAnswerId())
                .append("CreationDate", answer.getCreationDate())
                .append("Score", answer.getScore())
                .append("OwnerUserId", answer.getOwnerUserName());

        coll.updateOne(eq("PostId", postId), Updates.push("Answers", doc));

        return true;
    }

    public boolean insertPost(Post post){
        MongoCollection<Document> coll = mongoDatabase.getCollection("Post");

        Document doc = new Document("PostId", post.getPostId())
                .append("Title", post.gettitle())
                .append("Answers", post.getAnswers())
                .append("CreationDate", post.getCreationDate())
                .append("Body", post.getBody())
                .append("OwnerUserId", post.getOwnerUserId())
                .append("Tags", post.getTags());

        coll.insertOne(doc);

        return true;
    }

    public boolean insertUser(User user){
        boolean res = true;
        MongoCollection<Document> coll = mongoDatabase.getCollection("User");

        /* controllo con la checkUser se il displayName è già in uso */
        if(checkUser(user.getDisplayName())){
            res = false;
            System.out.println("displayName presente");
        }
        else {

            Document us = new Document("Id", user.getUserId())
                    .append("DisplayName", user.getDisplayName())
                    .append("Password", user.getPassword())
                    .append("CreationDate", user.getCreationData())
                    .append("Location", user.getLocation())
                    .append("AboutMe", user.getAboutMe())
                    .append("WebsiteURL", user.getWebsiteURL());

            coll.insertOne(us);
        }
        return res;
    }

    private boolean checkUser(String displayName) {
        MongoCollection<Document> coll = mongoDatabase.getCollection("User");
        boolean res = false;

        long count = coll.countDocuments(eq("DisplayName", displayName));

        if(count > 0)
            res = true;

        return res;
    }

    public boolean removeAnswer(Answer answer, String postId){
        MongoCollection<Document> coll = mongoDatabase.getCollection("Post");

        /*Document doc = new Document("AnswerId", answer.getAnswerId()).append("CreationDate", answer.getCreationDate()).append("Score", answer.getScore()).append("OwnerUserId", answer.getOwnerUserId());
        coll.updateOne(eq("PostId", postId), Updates.pull("Answers", doc));*/

        //provare uno dei due
        BasicDBObject match = new BasicDBObject("PostId", postId);
        BasicDBObject update = new BasicDBObject("Answers", new BasicDBObject("AnswerId", answer.getAnswerId()));
        coll.updateOne(match, new BasicDBObject("$pull", update));

        return true;
    }

    public boolean removePost(Post post){
        MongoCollection<Document> coll = mongoDatabase.getCollection("Post");
        coll.deleteOne(eq("PostId", post.getPostId()));
        return true;
    }

    public boolean removeUser(String userIdString){
        MongoCollection<Document> collUser = mongoDatabase.getCollection("Users");

        //addio utente
        collUser.deleteOne(eq("Id", userIdString));
        //addio post scritti da lui
        return true;
    }

    public boolean updateUserData(User user){
        MongoCollection<Document> coll = mongoDatabase.getCollection("User");
        coll.updateOne(
                eq("Id", user.getUserId()),
                and(
                        set("Password", user.getPassword()),
                        set("Location", user.getLocation()),
                        set("AboutMe", user.getAboutMe()),
                        set("WebsiteURL", user.getWebsiteURL())
                )
        );

        return true;
    }

    public void insertUserFollowerAndFollowedRelation(String userIdFollower, String userIdFollowed) {
        MongoCollection<Document> coll = mongoDatabase.getCollection("User");
        //TODO: Controllare se l'aggiornamento è corretto (differenza poco chiara tra followerNumber e followedNumber)
        coll.updateOne(eq("Id", userIdFollower), inc("followerNumber", 1));
        coll.updateOne(eq("Id", userIdFollowed), inc("followedNumber", 1));
    }

    public void removeUserFollowerAndFollowedRelation(String userIdFollower, String userIdFollowed) {
        MongoCollection<Document> coll = mongoDatabase.getCollection("User");
        //TODO: Controllare se l'aggiornamento è corretto (differenza poco chiara tra followerNumber e followedNumber)
        coll.updateOne(eq("Id", userIdFollower), inc("followerNumber", -1));
        coll.updateOne(eq("Id", userIdFollowed), inc("followedNumber", -1));
    }
}