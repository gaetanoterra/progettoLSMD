package server;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import javafx.util.Pair;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.set;

import client.*;
import middleware.*;

public class DocumentDBManager {

    private MongoClient dbConnection;
    private MongoDatabase database;

    public DocumentDBManager(){
        dbConnection = MongoClients.create("mongodb://localhost:27017");
        // dbConnection = MongoClients.create("mongodb://host-1:27020, host-2:27020, host-3:27020/?retryWrites=true&w=majority&wtimeout=10000");
        database = dbConnection.getDatabase("mydb");
    }

    public String[] findMostPopularTagsByLocation(String location, int numTags){
        //trovo tutti gli utenti relativi ad una locazione
        //scorro tutti i post che hanno ownerUserId tra gli utenti trovati prima
        //raggruppo per tag e li conto
        MongoCollection<Document> collPost = database.getCollection("server.Post");
        MongoCollection<Document> collUser = database.getCollection("server.User");
        ArrayList<String> tags = new ArrayList<>();


        ArrayList<User> user = new ArrayList<>();

        collUser.find(eq("location", location)).forEach(doc -> {
            User u = new User();
            u.setId(doc.getString("userId"));
            user.add(u);
        });

        /*try (MongoCursor<Document> cursor = collUser.find(eq("location", location)).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();
                User u = new User();

                //mi interessa solo lo userId
                u.setId(doc.getString("userId"));
                user.add(u);
            }
        }*/

        //adesso che ho la lista di utenti scorro i post e trovo quelli che hanno ownerUserId tra i miei
        Bson m = match(in("owneruserId", user));
        Bson u = unwind("tags");
        Bson g = group("$tags", sum("totaleTags",1));
        Bson s = sort(descending("totaleTags"));
        Bson l = limit(numTags);

        collUser.aggregate(Arrays.asList(m, u, g, s, l)).forEach(doc -> {
            tags.add(doc.getString("tags"));
        });

        /*try (MongoCursor<Document> cursor = collPost.aggregate(Arrays.asList(m, u, g, s, l)).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();

                tags.add(doc.getString("tags"));
            }
        }*/

        return (String[]) tags.toArray();
    }

    //restituisco gli id degli utenti più esperti
    public User[] findTopExpertsByTag(String tag, int num){
        MongoCollection<Document> collPost = database.getCollection("server.Post");
        MongoCollection<Document> collUser = database.getCollection("server.User");
        ArrayList<String> usersId = new ArrayList<>();
        ArrayList<User> user = new ArrayList<>();

        Bson m = match(in("tags", tag));
        Bson u = unwind("answers");
        Bson g = group("$answers.ownerUserId", sum("totaleRisposteUtente",1));
        Bson s = sort(descending("totaleTags"));
        Bson l = limit(num);
        //Bson p = project(fields(include("$answers.ownerUserId")));

        try (MongoCursor<Document> cursor = collPost.aggregate(Arrays.asList(m, u, g, s, l)).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();

                usersId.add(doc.getString("answers.ownerUserId"));
            }
        }

        collUser.find(in("userId", (String[])usersId.toArray())).forEach(document ->{
            User us = new User()
                    .setId(document.getString("userId"))
                    .setDisplayName(document.getString("displayName"))
                    .setPassword(document.getString("password"))
                    .setFollowersNumber(document.getInteger("followersNumber"))
                    .setFollowedNumber(document.getInteger("followedNumber"))
                    .setReputation(document.getDouble("reputation"))
                    .setCreationData(document.getDate("creationDate"))
                    .setLastAccessDate(document.getDate("lastAccessDate"))
                    .setType(document.getString("type"))
                    .setLocation(document.getString("location"))
                    .setAboutMe(document.getString("aboutMe"))
                    .setWebsiteURL(document.getString("websiteURL"));
            user.add(us);
        });

        return (User[]) user.toArray();
    }

    public Map<User, Pair<String,Integer>[]> findHotTopicsForTopUsers(){
        // non riesco a ricordare quale era il metodo per completare questa operazione, quindi lo lascio qui
        /*
        3)Trova i 50 utenti più seguiti,
        e per ogni utente mostra
        i 3 tag per cui hanno scritto più risposte
        (a scopo di trovare gli hooooooooooot topics, tag più popolari nella top 3 degli utenti)
         */
        MongoCollection<Document> collPost = database.getCollection("server.Post");
        MongoCollection<Document> collUser = database.getCollection("server.User");
        HashMap<User, Pair<String,Integer>[]> result = new HashMap<>();
        /*
        db.users.aggregate([
            {$sort: {FollowersNumber: -1}},
            {$limit: 50}
        ])
        //50 at most users, the most followed ones
        */
        Bson a = sort(descending("FollowersNumber"));
        Bson b = limit(50);
        collUser.aggregate(Arrays.asList(a,b)).forEach(document -> {
            User user = new User()
                .setId(document.getString("userId"))
                .setDisplayName(document.getString("displayName"))
                .setPassword(document.getString("password"))
                .setFollowersNumber(document.getInteger("followersNumber"))
                .setFollowedNumber(document.getInteger("followedNumber"))
                .setReputation(document.getDouble("reputation"))
                .setCreationData(document.getDate("creationDate"))
                .setLastAccessDate(document.getDate("lastAccessDate"))
                .setType(document.getString("type"))
                .setLocation(document.getString("location"))
                .setAboutMe(document.getString("aboutMe"))
                .setWebsiteURL(document.getString("websiteURL"));
            // user done, now the three posts
            // for each user id ($userId)
            int userId = Integer.parseInt(user.getUserId());
            Bson c = match(eq("Answers.OwnerUserId", userId)); //ownerUserId è di tipo String
            Bson d = unwind("$Answers");
            Bson e = unwind("$Tags");
            Bson f = new Document("$group",
                    new Document("_id", "$Tags").append("count",
                            new Document("$sum", 1)));
            Bson g = sort(descending("count"));
            Bson h = limit(3);
            Bson i = project(fields(Projections.computed("tag","_id"), include("count")));
                /*
                db.posts.aggregate([
                        {$match:
                {'Answers.OwnerUserId' : $userId}
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
            collPost.aggregate(Arrays.asList(c, d, e, f, g, h, i)).forEach(doc ->
                list.add(new Pair<>(doc.getString("tag"), doc.getInteger("count")))
            );
            result.put(user, (Pair<String, Integer>[]) list.toArray());
        });
        return result;
    }

    public Post getPostById(String postId){
        MongoCollection<Document> coll = database.getCollection("server.Post");

        Document postDoc = coll.find(eq("postId", postId)).first();

        return new Post(postId,
                postDoc.getString("title"),
                (ArrayList<Answer>)postDoc.get("answers"),
                postDoc.getDate("creationDate"),
                postDoc.getString("body"),
                postDoc.getString("ownerUserId"),
                (ArrayList<String>)postDoc.get("tags"));
    }

    public Post[] getPostsByTag(String[] tags){
        MongoCollection<Document> coll = database.getCollection("server.Post");

        ArrayList<Post> posts = new ArrayList<>();
        try (MongoCursor<Document> cursor = coll.find(all("tags", tags)).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();
                Post p = new Post(doc.getString("postId"),
                                  doc.getString("title"),
                                  (ArrayList<Answer>)doc.get("answers"),
                                  doc.getDate("creationDate"),
                                  doc.getString("body"),
                                  doc.getString("ownerUserId"),
                                  (ArrayList<String>)doc.get("tags"));

                posts.add(p);
            }
        }

        return (Post[]) posts.toArray();
    }

    public Post[] getPostsByText(String text){
        // controllo il titolo per semplicità (e velocità), si può cambiare ovviamente con il body
        MongoCollection<Document> coll = database.getCollection("server.Post");
        ArrayList<Post> list = new ArrayList<>();
        coll.find(new Document("title", new Document("$regex", ".*"+text+".*"))).forEach(doc -> {
            Post p = new Post(doc.getString("postId"),
                    doc.getString("title"),
                    (ArrayList<Answer>)doc.get("answers"),
                    doc.getDate("creationDate"),
                    doc.getString("body"),
                    doc.getString("ownerUserId"),
                    (ArrayList<String>)doc.get("tags"));
            list.add(p);
        });
        return (Post[]) list.toArray();
    }

    public User getUserData(String displayName){
        MongoCollection<Document> coll = database.getCollection("server.User");

        Document userDoc = coll.find(eq("displayName", displayName)).first();
        User user = new User();

        if(userDoc != null) {
            user.setId(userDoc.getString("userId"))
                .setDisplayName(displayName)
                .setPassword(userDoc.getString("password"))
                .setFollowersNumber(userDoc.getInteger("followersNumber"))
                .setFollowedNumber(userDoc.getInteger("followedNumber"))
                .setReputation(userDoc.getDouble("reputation"))
                .setCreationData(userDoc.getDate("creationDate"))
                .setLastAccessDate(userDoc.getDate("lastAccessDate"))
                .setType(userDoc.getString("type"))
                .setLocation(userDoc.getString("location"))
                .setAboutMe(userDoc.getString("aboutMe"))
                .setWebsiteURL(userDoc.getString("websiteURL"));
        }

        return user;
    }

    public User[] getUsersRank(){
        MongoCollection<Document> coll = database.getCollection("server.User");

        ArrayList<User> user = new ArrayList<>();
        try (MongoCursor<Document> cursor = coll.find().sort(descending("reputation")).limit(10).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();
                User u = new User();

                u.setId(doc.getString("userId"))
                    .setDisplayName(doc.getString("displayName"))
                    .setPassword(doc.getString("password"))
                    .setFollowersNumber(doc.getInteger("followersNumber"))
                    .setFollowedNumber(doc.getInteger("followedNumber"))
                    .setReputation(doc.getDouble("reputation"))
                    .setCreationData(doc.getDate("creationDate"))
                    .setLastAccessDate(doc.getDate("lastAccessDate"))
                    .setType(doc.getString("type"))
                    .setLocation(doc.getString("location"))
                    .setAboutMe(doc.getString("aboutMe"))
                    .setWebsiteURL(doc.getString("websiteURL"));

                user.add(u);
            }
        }

        return (User[]) user.toArray();
    }

    public boolean insertAnswer(Answer answer, String postId){
        MongoCollection<Document> coll = database.getCollection("server.Post");

        Document doc = new Document("answerId", answer.getAnswerId()).
                                    append("creationDate", answer.getCreationDate()).
                                    append("score", answer.getScore()).
                                    append("ownerUserId", answer.getOwnerUserId());

        coll.updateOne(eq("postId", postId), Updates.push("answers", doc));

        return true;
    }

    /////////////////////////////////////////////////////////////
    /////////// da levare ///////////////////////////////////////
    /////////////////////////////////////////////////////////////
    /*public boolean insertFollowRelationAndUpdate(String displayNameFollower, String displayNameFollowed){
        MongoCollection<Document> coll = database.getCollection("server.Post");


    }*/

    public boolean insertPost(Post post){
        MongoCollection<Document> coll = database.getCollection("server.Post");

        Document doc = new Document("postId", post.getPostId())
                    .append("title", post.getTitle())
                    .append("answers", post.getAnswers())
                    .append("creationDate", post.getCreationDate())
                    .append("body", post.getBody())
                    .append("ownerUserId", post.getOwnerUserId())
                    .append("tags", post.getTags());

        coll.insertOne(doc);

        return true;
    }

    public boolean insertUser(User user){
        boolean res = true;
        MongoCollection<Document> coll = database.getCollection("server.User");

        /* controllo con la checkUser se il displayName è già in uso */
        if(checkUser(user.getDisplayName())){
            res = false;
            System.out.println("displayName presente");
        }
        else {
            Document us = new Document("userId", user.getUserId())
                    .append("displayName", user.getDisplayName())
                    .append("password", user.getPassword())
                    .append("creationDate", user.getCreationData())
                    .append("location", user.getLocation())
                    .append("aboutMe", user.getAboutMe())
                    .append("websiteURL", user.getWebsiteURL());

            coll.insertOne(us);
        }
        return res;
    }

    private boolean checkUser(String displayName) {
        MongoCollection<Document> coll = database.getCollection("server.User");
        boolean res = false;

        long count = coll.countDocuments(eq("displayName", displayName));

        if(count > 0)
            res = true;

        return res;
    }

    /////////////////////////////////////////////////////////////
    /////////// da levare ///////////////////////////////////////
    /////////////////////////////////////////////////////////////
    /*public boolean insertVote(int postId, int answerId, String displayName, int voto){

    }*/

    public boolean removeAnswer(Answer answer, String postId){
        MongoCollection<Document> coll = database.getCollection("server.Post");

        /*Document doc = new Document("answerId", answer.getAnswerId()).append("creationDate", answer.getCreationDate()).append("score", answer.getScore()).append("ownerUserId", answer.getOwnerUserId());

        coll.updateOne(eq("postId", postId), Updates.pull("answers", doc));*/

        //provare uno dei due
        BasicDBObject match = new BasicDBObject("postId", postId);
        BasicDBObject update = new BasicDBObject("answers", new BasicDBObject("answerId", answer.getAnswerId()));
        coll.updateOne(match, new BasicDBObject("$pull", update));

        return true;
    }

    /////////////////////////////////////////////////////////////
    /////////// da levare ///////////////////////////////////////
    /////////////////////////////////////////////////////////////
    /*public boolean removeFollowRelationAndUpdate(String displayNameFollower, String displayNameFollowed){

    }*/

    public boolean removePost(Post post){
        MongoCollection<Document> coll = database.getCollection("server.Post");

        coll.deleteOne(eq("postId", post.getPostId()));

        return true;
    }

    public boolean removeUser(String displayName){
        MongoCollection<Document> collUser = database.getCollection("server.User");
        MongoCollection<Document> collPost = database.getCollection("server.Post");

        collUser.deleteOne(eq("displayName", displayName));
        collPost.deleteMany(eq("ownerUserId", displayName));

        return true;
    }

    public boolean updateUserData(User user){
        MongoCollection<Document> coll = database.getCollection("server.User");

        coll.updateOne(eq("userId", user.getUserId()), and(set("password", user.getPassword()),
                                                                    set("location", user.getLocation()),
                                                                    set("aboutMe", user.getAboutMe()),
                                                                    set("websiteURL", user.getWebsiteURL())));

        return true;
    }
}
