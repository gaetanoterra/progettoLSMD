package server;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Updates.set;

import client.*;
import middleware.*;

public class DocumentDBManager {

    private MongoClient dbConnection;
    private MongoDatabase database;

    public DocumentDBManager(){
        dbConnection = MongoClients.create("mongodb://localhost:27017");
        database = dbConnection.getDatabase("mydb");
    }

    public ArrayList<String> findMostPopularTagsByLocation(String location, int numTags){
        //trovo tutti gli utenti relativi ad una locazione
        //scorro tutti i post che hanno ownerUserId tra gli utenti trovati prima
        //raggruppo per tag e li conto
        MongoCollection<Document> collPost = database.getCollection("server.Post");
        MongoCollection<Document> collUser = database.getCollection("server.User");
        ArrayList<String> tags = new ArrayList<>();


        ArrayList<User> user = new ArrayList<User>();
        try (MongoCursor<Document> cursor = collUser.find(eq("location", location)).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();
                User u = new User();

                //mi interessa solo lo userId
                u.setId(doc.getString("userId"));
                user.add(u);
            }
        }

        //adesso che ho la lista di utenti scorro i post e trovo quelli che hanno ownerUserId tra i miei
        Bson m = match(in("owneruserId", user));
        Bson u = unwind("tags");
        Bson g = group("$tags", sum("totaleTags",1));
        Bson s = sort(descending("totaleTags"));
        Bson l = limit(10);

        try (MongoCursor<Document> cursor = collPost.aggregate(Arrays.asList(m, u, g, s, l)).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();

                tags.add(doc.getString("tags"));
            }
        }

        return tags;
    }

    //restituisco gli id degli utenti più esperti
    public ArrayList<String> findTopExpertsByTag(String tag, int num){
        MongoCollection<Document> collPost = database.getCollection("server.Post");
        MongoCollection<Document> collUser = database.getCollection("server.User");
        ArrayList<String> usersId = new ArrayList<>();

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

        return usersId;
    }

    public Post getPostById(String postId){
        MongoCollection<Document> coll = database.getCollection("server.Post");

        Document postDoc = coll.find(eq("postId", postId)).first();
        Post post = new Post(postId,
                             postDoc.getString("title"),
                             (ArrayList<Answer>)postDoc.get("answers"),
                             postDoc.getDate("creationDate"),
                             postDoc.getString("body"),
                             postDoc.getString("ownerUserId"),
                             (ArrayList<String>)postDoc.get("tags"));

        return post;
    }

    public ArrayList<Post> getPostsByTag(String[] tags){
        MongoCollection<Document> coll = database.getCollection("server.Post");

        ArrayList<Post> posts = new ArrayList<Post>();
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

        return posts;
    }

    public Post getPostByText(String text){

    }

    public User getUserData(String displayName){
        MongoCollection<Document> coll = database.getCollection("server.User");

        Document userDoc = coll.find(eq("displayName", displayName)).first();
        User user = new User();

        if(userDoc != null) {
            user.setId(userDoc.getString("userId"));
            user.setDisplayName(displayName);
            user.setPassword(userDoc.getString("password"));
            user.setFollowersNumber(userDoc.getInteger("followersNumber"));
            user.setFollowedNumber(userDoc.getInteger("followedNumber"));
            user.setReputation(userDoc.getDouble("reputation"));
            user.setCreationData(userDoc.getDate("creationDate"));
            user.setLastAccessDate(userDoc.getDate("lastAccessDate"));
            user.setType(userDoc.getString("type"));
            user.setLocation(userDoc.getString("location"));
            user.setAboutMe(userDoc.getString("aboutMe"));
            user.setWebsiteURL(userDoc.getString("websiteURL"));
        }

        return user;
    }

    public ArrayList<User> getUsersRank(){
        MongoCollection<Document> coll = database.getCollection("server.User");

        ArrayList<User> user = new ArrayList<User>();
        try (MongoCursor<Document> cursor = coll.find().sort(descending("reputation")).limit(10).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();
                User u = new User();

                u.setId(doc.getString("userId"));
                u.setDisplayName(doc.getString("displayName"));
                u.setPassword(doc.getString("password"));
                u.setFollowersNumber(doc.getInteger("followersNumber"));
                u.setFollowedNumber(doc.getInteger("followedNumber"));
                u.setReputation(doc.getDouble("reputation"));
                u.setCreationData(doc.getDate("creationDate"));
                u.setLastAccessDate(doc.getDate("lastAccessDate"));
                u.setType(doc.getString("type"));
                u.setLocation(doc.getString("location"));
                u.setAboutMe(doc.getString("aboutMe"));
                u.setWebsiteURL(doc.getString("websiteURL"));

                user.add(u);
            }
        }

        return user;
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
        MongoCollection<Document> coll = database.getCollection("Users");

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
        MongoCollection<Document> coll = database.getCollection("Users");
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
        MongoCollection<Document> coll = database.getCollection("server.User");

        coll.deleteOne(eq("displayName", displayName));

        return true;
    }

    public boolean updateUserData(User user){
        MongoCollection<Document> coll = database.getCollection("server.User");

        coll.updateOne(eq("userId", user.getUserId()), and(set("password", user.getPassword()), set("location", user.getLocation()), set("aboutMe", user.getAboutMe()), set("websiteURL", user.getWebsiteURL())));

        return true;
    }
}
