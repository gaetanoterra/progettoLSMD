import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Indexes.descending;
import static com.mongodb.client.model.Updates.set;

public class DocumentDBManager {

    private MongoClient dbConnection;
    private MongoDatabase database;

    public DocumentDBManager(){
        dbConnection = MongoClients.create("mongodb://localhost:27017");
        database = dbConnection.getDatabase("mydb");
    }

    public User[] findMostPopularTagsByLocation(String tag, int idUser){

    }

    public User[] findTopExpertsByTag(String tag, int idUser){

    }

    public Post getPostById(String postId){
        MongoCollection<Document> coll = database.getCollection("Post");

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
        MongoCollection<Document> coll = database.getCollection("Post");

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

    public User GetUserData(String username){
        MongoCollection<Document> coll = database.getCollection("User");

        Document userDoc = coll.find(eq("username", username)).first();
        User user = new User();

        user.setId(userDoc.getString("userId"));
        user.setUsername(username);
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

        return user;
    }

    public ArrayList<User> getUsersRank(){
        MongoCollection<Document> coll = database.getCollection("User");

        ArrayList<User> user = new ArrayList<User>();
        try (MongoCursor<Document> cursor = coll.find().sort(descending("reputation")).limit(10).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();
                User u = new User();

                u.setId(doc.getString("userId"));
                u.setUsername(doc.getString("username"));
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
        MongoCollection<Document> coll = database.getCollection("Post");

        Document doc = new Document("answerId", answer.getAnswerId()).append("creationDate", answer.getCreationDate()).append("score", answer.getScore()).append("ownerUserId", answer.getOwnerUserId());

        coll.updateOne(eq("postId", postId), Updates.push("answers", doc));

        return true;
    }

    public boolean insertFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        MongoCollection<Document> coll = database.getCollection("Post");


    }

    public boolean insertPost(Post post){
        MongoCollection<Document> coll = database.getCollection("Post");

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

        /* controllo con la checkUser se lo username è già in uso */
        if(checkUser(user.getUsername())){
            res = false;
            System.out.println("Username presente");
        }
        else {
            Document us = new Document("userId", user.getUserId())
                    .append("username", user.getUsername())
                    .append("password", user.getPassword())
                    .append("creationDate", user.getCreationData())
                    .append("location", user.getLocation())
                    .append("aboutMe", user.getAboutMe())
                    .append("websiteURL", user.getWebsiteURL());

            coll.insertOne(us);
        }
        return res;
    }

    private boolean checkUser(String username) {
        MongoCollection<Document> coll = database.getCollection("Users");
        boolean res = false;

        long count = coll.countDocuments(eq("username", username));

        if(count > 0)
            res = true;

        return res;
    }

    public boolean insertVote(int postId, int answerId, String username, int voto){

    }

    public boolean removeAnswer(Answer answer, String postId){
        MongoCollection<Document> coll = database.getCollection("Post");

        /*Document doc = new Document("answerId", answer.getAnswerId()).append("creationDate", answer.getCreationDate()).append("score", answer.getScore()).append("ownerUserId", answer.getOwnerUserId());

        coll.updateOne(eq("postId", postId), Updates.pull("answers", doc));*/

        //provare uno dei due
        BasicDBObject match = new BasicDBObject("postId", postId);
        BasicDBObject update = new BasicDBObject("answers", new BasicDBObject("answerId", answer.getAnswerId()));
        coll.updateOne(match, new BasicDBObject("$pull", update));

        return true;
    }

    public boolean removeFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){

    }

    public boolean removePost(Post post){
        MongoCollection<Document> coll = database.getCollection("Post");

        coll.deleteOne(eq("postId", post.getPostId()));

        return true;
    }

    public boolean removeUser(String username){
        MongoCollection<Document> coll = database.getCollection("User");

        coll.deleteOne(eq("username", username));

        return true;
    }

    public boolean updateUserData(User user){
        MongoCollection<Document> coll = database.getCollection("User");

        coll.updateOne(eq("userId", user.getUserId()), and(set("password", user.getPassword()), set("location", user.getLocation()), set("aboutMe", user.getAboutMe()), set("websiteURL", user.getWebsiteURL())));

        return true;
    }
}
