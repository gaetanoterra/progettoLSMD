package it.unipi.dii.server.databaseDriver;

import com.mongodb.MongoWriteException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertOneResult;
import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
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

import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;

//TODO: Necessaria revisione dei metodi per verificare se sono stati implementati nella loro completezza
public class DocumentDBManager {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private final String POSTSCOLLECTION = "Posts";
    private final String USERSCOLLECTION = "Users";
    //TODO: Da rimuovere se necessario
    private final String IdMetadata = "MetadataDocument";
    //TODO: Da rimuovere se necessario
    private final String IdValueMetadata = "LastId";
    private MongoCollection<Document> postsCollection;
    private MongoCollection<Document> usersCollection;

    //indici su mongodb
    private final String indexes = """
        db.Users.createIndex({DisplayName:1}, { unique: true } )
        db.Posts.createIndex({Title:"text",Body:"text"});
        """;

    private void init(){
        postsCollection.createIndex(Indexes.compoundIndex(
                                                Indexes.text("Title"),
                                                Indexes.text("Body")
                                    )
        );
        usersCollection.createIndex(new Document("DisplayName",1), new IndexOptions().unique(true));
        postsCollection.createIndex(new Document("GlobalPostId",1), new IndexOptions().unique(true));
    }
    public DocumentDBManager(){
        this(DBExecutionMode.LOCAL);
    }

    public DocumentDBManager(DBExecutionMode dbe){
        switch (dbe) {
            case LOCAL   -> mongoClient = MongoClients.create("mongodb://localhost:27017");
            case CLUSTER -> mongoClient = MongoClients.create("mongodb://172.16.4.117:27017,172.16.4.118:27017,172.16.4.119:27017/?replicaSet=pseudostackoverdb&retryWrites=true&w=majority&wtimeout=10000");
        }
        mongoDatabase = mongoClient.getDatabase("PseudoStackOverDB");
        postsCollection = mongoDatabase.getCollection(POSTSCOLLECTION);
        usersCollection = mongoDatabase.getCollection(USERSCOLLECTION);
        init();
    }

    public void close(){
        this.mongoClient.close();
    }


    //restituisco gli id degli utenti più esperti
    public String[] findTopExpertsByTag(String tag, int num){
        ArrayList<String> userIdList = new ArrayList<>();

        Bson matchTag = match(eq("Tags", tag));
        Bson unwindAnswers = unwind("$Answers");
        //raggruppando su un attributo, questo dovrebbe diventare _id, e perde il nome originale
        Bson groupByOwnerUserId = group("$Answers.OwnerDisplayName", sum("totaleRisposteUtente",1));
        Bson sortByCountDesc = sort(descending("totaleRisposteUtente"));
        Bson limitStage = limit(num);

        postsCollection.aggregate(
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
        return ((userIdList.toArray(new String[userIdList.size()]).length == 0)? null : userIdList.toArray(new String[userIdList.size()]));
    }


    public Post getPostById(String globalPostId){

        List<Answer> answersList = new ArrayList<>();

        Document document = this.postsCollection.find(eq("GlobalPostId", globalPostId)).first();

        if(document == null) {
            System.out.println("Found no document marching the id " + globalPostId);
            return new Post();
        }else{
            this.increaseViewsPost(globalPostId);

            document.getList("Answers", Document.class).forEach((answerDocument) -> {
                answersList.add(
                    new Answer(
                        answerDocument.getString("answerId"),
                        answerDocument.getLong("CreationDate"),
                        answerDocument.getInteger("Score"),
                        answerDocument.getString("OwnerUserId"),
                        answerDocument.getString("DisplayName"),
                        answerDocument.getString("Body"),
                        document.getObjectId("_id").toString()
                    )
                );
            });

            Post post = new Post(
                    document.getObjectId("_id").toString(),
                    globalPostId,
                    document.getString("Title"),
                    answersList,
                    document.getLong("CreationDate"),
                    document.getString("Body"),
                    document.getString("OwnerUserId"),
                    document.getString("DisplayName"),
                    document.getList("Tags", String.class)
            );
            post.setOwnerUserName(document.getString("DisplayName"));
            return post;
        }

    }

    private void increaseViewsPost(String globalPostId) {
        this.postsCollection.updateOne(eq("GlobalPostId", globalPostId), inc("ViewCount", 1));
    }

    public ArrayList<Post> getPostsByOwnerUsername(String username) {

        ArrayList<Post> posts = new ArrayList<>();
        postsCollection.find(all("DisplayName", username)).forEach(doc -> {

            List<Answer> answersList = new ArrayList<>();
            doc.getList("Answers", Document.class).forEach((answerDocument) -> {
                answersList.add(
                        new Answer(
                                answerDocument.getString("Id"),
                                answerDocument.getLong("CreationDate"),
                                answerDocument.getInteger("Score"),
                                answerDocument.getString("OwnerUserId"),
                                answerDocument.getString("OwnerDisplayName"),
                                answerDocument.getString("Body"),
                                doc.getObjectId("_id").toHexString()
                        )
                );
            });
            Post p = new Post(
                    doc.getObjectId("_id").toString(),
                    doc.getString("GlobalPostId"),
                    doc.getString("Title"),
                    answersList,
                    doc.getLong("CreationDate"),
                    doc.getString("Body"),
                    doc.getString("OwnerUserId"),
                    doc.getString("DisplayName"),
                    doc.getList("Tags", String.class)
            ).setViews(doc.getInteger("ViewCount"));

            posts.add(p);
        });

        System.out.println("getPostsByOwnerUsername: found " + posts.size() + " posts matching the username " + username);
        return posts;
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
                _id:    1,
                Title:  1,
                Tags:   1,
                numberOfAnswers: { $size: "$Answers" },
                ViewCount:  1
            }
        );
    */
    public ArrayList<Post> getPostsByText(String text){
        ArrayList<Post> postArrayList = new ArrayList<>();
        this.postsCollection.find(and(
                            Filters.text(text),
                            or(
                               regex("Title",".*" +text +".*","i"),
                               regex("Body" ,".*" +text +".*","i")
                            ))
                        )
                        .projection(new Document("Title",1)
                                    .append("GlobalPostId", 1)
                                    .append("_id", 1)
                                    .append("ViewCount", 1)
                                    .append("OwnerUserId", 1)
                                    .append("DisplayName", 1)
                                    .append("Tags", 1)
                                    .append("AnswersNumber", new BasicDBObject("$size","$Answers"))
                        )
                        .forEach(doc -> {
                            Post p = new Post(doc.getObjectId("_id").toString(),
                                              doc.getString("GlobalPostId"),
                                              doc.getString("Title"),
                                              doc.getInteger("AnswersNumber"),
                                              doc.getString("OwnerUserId"),
                                              doc.getList("Tags", String.class))
                                    .setOwnerUserName(doc.getString("DisplayName"))
                                    .setViews(doc.getInteger("ViewCount"));
                            postArrayList.add(p);
                        }
        );

        System.out.println("found " + postArrayList.size() + " posts matching the text given as input");
        return postArrayList;
    }
    public User getUserDataByUsername(String displayName){

        Document userDoc = usersCollection.find(eq("DisplayName", displayName)).first();
        User user = new User();

        if(userDoc != null) {
            user.setUserId(userDoc.getObjectId("_id").toString())
                    .setDisplayName(displayName)
                    .setPassword(userDoc.getString("Password"))
                    .setFollowersNumber(userDoc.getInteger("followerNumber"))
                    .setFollowedNumber(userDoc.getInteger("followedNumber"))
                    .setReputation(userDoc.getInteger("Reputation"))
                    .setCreationDate(userDoc.getLong("CreationDate"))
                    .setLastAccessDate(userDoc.getLong("LastAccessDate"))
                    .setIsAdmin(userDoc.getBoolean("IsAdmin", false))
                    .setLocation(userDoc.getString("Location"))
                    .setAboutMe(userDoc.getString("AboutMe"))
                    .setWebsiteURL(userDoc.getString("WebsiteUrl"))
                    .setProfileImage(userDoc.getString("ProfileImageUrl"));

            if(userDoc.getBoolean("IsAdmin") != null && userDoc.getBoolean("IsAdmin") == true)
                user.setIsAdmin(true);
        }

        return user;
    }


    public User[] getUsersRank(){

        ArrayList<User> user = new ArrayList<>();
        usersCollection.find().sort(descending("Reputation")).limit(10).forEach(document -> {
            User u = new User()
                    .setUserId(document.getObjectId("_id").toString())
                    .setDisplayName(document.getString("DisplayName"))
                    .setPassword(document.getString("Password"))
                    .setFollowersNumber(document.getInteger("followerNumber"))
                    .setFollowedNumber(document.getInteger("followedNumber"))
                    .setReputation(document.getInteger("Reputation"))
                    .setCreationDate(document.getLong("CreationDate"))
                    .setLastAccessDate(document.getLong("LastAccessDate"))
                    .setIsAdmin(document.getBoolean("IsAdmin", false))
                    .setLocation(document.getString("Location"))
                    .setAboutMe(document.getString("AboutMe"))
                    .setWebsiteURL(document.getString("WebsiteUrl"));

            user.add(u);
        });

        return user.toArray(new User[user.size()]);
    }

    /*
    db.Posts.updateOne(
        { _id: ??},
        {$push: {
                Answers:{
                    answerId: ??,
                    Body: ??,
                    CreationDate: ??,
                    OwnerDisplayName:??,
                    Score:0
                }
            }
        }
    );
     */
    public boolean insertAnswer(Answer answer){
        Document answerDocument = new Document()
                .append("answerId", answer.getAnswerId())
                .append("Body", answer.getBody())
                .append("CreationDate", answer.getCreationDate())
                .append("OwnerDisplayName", answer.getOwnerUserName())
                .append("Score", 0);
        return (postsCollection.updateOne(
                Filters.eq("GlobalPostId", answer.getParentPostId()),
                Updates.push("Answers", answerDocument)
        ).getModifiedCount()> 0);

    }

    public boolean insertPost(Post post){

        Document doc = new Document()
                .append("GlobalPostId", post.getGlobalId())
                .append("Title", post.getTitle())
                .append("Answers", post.getAnswers())
                .append("CreationDate", post.getCreationDate())
                .append("Body", post.getBody())
                .append("OwnerUserId", post.getOwnerUserId())
                .append("DisplayName", post.getOwnerUserName())
                .append("Tags", post.getTags())
                .append("ViewCount", post.getViews());

        try {
            InsertOneResult result = postsCollection.insertOne(doc);
            // set post id as the objectId
            return result.wasAcknowledged();
        }
        catch (MongoWriteException mwe) {
            System.out.println("Post " + post.getMongoPost_id() + " already exists");
            return false;
        }
    }

    public boolean insertUser(User user){
        Document userDoc = new Document("DisplayName", user.getDisplayName())
                .append("Password", user.getPassword())
                .append("CreationDate", user.getCreationDate())
                .append("LastAccessDate", user.getLastAccessDate())
                .append("Location", user.getLocation())
                .append("AboutMe", user.getAboutMe())
                .append("WebsiteUrl", user.getWebsiteURL())
                .append("followedNumber", user.getFollowedNumber())
                .append("followerNumber", user.getFollowersNumber())
                .append("Reputation", user.getReputation())
                ; //.append("type", user.getType())

        try {
            InsertOneResult result = usersCollection.insertOne(userDoc);
            return result.wasAcknowledged();
        }
        catch (MongoWriteException mwe) {
            System.out.println("Username " + user.getDisplayName() + " already exists");
            return false;
        }
    }

    //
    public boolean checkUser(String displayName) {
        boolean res = false;

        long count = usersCollection.countDocuments(eq("DisplayName", displayName));

        if(count > 0)
            res = true;

        return res;
    }

    public boolean removeAnswer(Answer answer){
        // ho bisogno di eliminare la risposta, ma anche di aggiornare l'attributo della reputation dell'utente
        // atomicamente recupero la risposta e la elimino dal post
        String answerId = answer.getAnswerId();
        Document beforeRemoveDocument = postsCollection.findOneAndUpdate(
                and(eq("_id", answer.getParentPostId()), eq("Answers.answerId", answerId)),
                new Document("$pull",
                        new Document("Answers",
                                new Document("Id", answerId)
                        )
                )
        );
        // ora posso recuperare lo score e aggiornare la reputation con l'opposto dello score (così annullo i voti fatti sulla risposta)
        for (Document answerDoc: beforeRemoveDocument.getList("Answers", Document.class)) {
            if (answerDoc.getString("Id").equals(answerId)) {
                usersCollection.updateOne(eq("Id", answerDoc.getString("OwnerUserId")), inc("Reputation", -answerDoc.getInteger("Score")));
                break;
            }
        }

        return true;
    }

    public boolean removePost(Post post){
        // ho bisogno di eliminare la risposta, ma anche di aggiornare l'attributo della reputation dell'utente
        // rimuovo atomicamente il post
        Document beforeRemoveDocument = postsCollection.findOneAndDelete(eq("GlobalPostId", post.getGlobalId()));
        // ora posso recuperare lo score e aggiornare la reputation con l'opposto dello score (così annullo i voti fatti sulla risposta)
        // questa operazione va fatta su tutti gli utenti che hanno risposto
        for (Document answerDoc: beforeRemoveDocument.getList("Answers", Document.class)) {
            usersCollection.updateOne(eq("Id", answerDoc.getString("OwnerUserId")), inc("Reputation", -answerDoc.getInteger("Score")));
        }

        return true;
    }

    public boolean removeUser(String displayName){
        return (usersCollection.deleteOne(eq("DisplayName", displayName)).getDeletedCount() > 0);
    }

    public boolean updateUserData(User user){
        usersCollection.updateOne(
                eq("_id", new ObjectId(user.getUserId())),
                Updates.combine(
                        set("Password", user.getPassword()),
                        set("Location", user.getLocation()),
                        set("AboutMe", user.getAboutMe()),
                        set("WebsiteUrl", user.getWebsiteURL())
                )
        );

        return true;
    }

    public void updateVotesAnswerAndReputation(String postId, String answerId, int changeVote) {
        postsCollection.updateOne(
                and(eq("GlobalPostId", postId), eq("Answers.answerId", answerId)),
                Updates.inc("Answers.$.Score", changeVote)
        );
        Document postDocument = postsCollection.find(and(eq("GlobalPostId", postId), eq("Answers.answerId", answerId)))
                .first();
        if (postDocument == null) {
            return;
        }
        List<Document> answerList = postDocument.getList("Answers", Document.class);
        for (Document answer: answerList) {
            if (answer.getString("answerId").equals(answerId)) {
                usersCollection.updateOne(eq("Id", answer.getString("OwnerUserId")), inc("Reputation", changeVote));
                break;
            }
        }

    }


    public void insertUserFollowerAndFollowedRelation(String userIdFollower, String userIdFollowed) {
        //TODO: Controllare se l'aggiornamento è corretto (differenza poco chiara tra followerNumber e followedNumber)
        usersCollection.updateOne(eq("DisplayName", userIdFollower), inc("followedNumber", 1));
        usersCollection.updateOne(eq("DisplayName", userIdFollowed), inc("followerNumber", 1));
    }

    public void removeUserFollowerAndFollowedRelation(String userIdFollower, String userIdFollowed) {
        //TODO: Controllare se l'aggiornamento è corretto (differenza poco chiara tra followerNumber e followedNumber)
        usersCollection.updateOne(eq("DisplayName", userIdFollower), inc("followedNumber", -1));
        usersCollection.updateOne(eq("DisplayName", userIdFollowed), inc("followerNumber", -1));
    }



    /*
          db.Posts.aggregate(
              [
                  { $unwind: { path:"$Tags" } },
                  { $group:  { _id:"$Tags", tagsNo: { $count: {} } } },
                  { $sort:   { "tagsNo": -1 } }
              ]
          );
          [
            { _id: 'c#', tagsNo: 9540 },
            { _id: '.net', tagsNo: 7174 },
            { _id: 'java', tagsNo: 5284 },
            { _id: 'asp.net', tagsNo: 4649 },
            { _id: 'c++', tagsNo: 3830 },
            { _id: 'javascript', tagsNo: 3409 },
            { _id: 'php', tagsNo: 2794 },
            { _id: 'python', tagsNo: 2606 },
            { _id: 'sql-server', tagsNo: 2537 },
            { _id: 'sql', tagsNo: 2523 },
            { _id: 'windows', tagsNo: 1842 },
            { _id: 'html', tagsNo: 1770 },
            { _id: 'visual-studio', tagsNo: 1529 },
            { _id: 'database', tagsNo: 1508 },
            { _id: 'mysql', tagsNo: 1464 },
            { _id: 'c', tagsNo: 1437 },
            { _id: 'jquery', tagsNo: 1279 },
            { _id: 'asp.net-mvc', tagsNo: 1216 },
            { _id: 'css', tagsNo: 1185 },
            { _id: 'xml', tagsNo: 1176 }
          ]
      */
    public Map<String,Integer> findMostPopularTags(){
        HashMap<String,Integer> mostPopularTagsHashMap = new HashMap<>();
        postsCollection.aggregate(
                Arrays.asList(
                        Aggregates.unwind("$Tags"),
                        Aggregates.group("$Tags", new BsonField("tagsNo", Aggregates.count())),
                        Aggregates.sort(Sorts.descending("tagsNo"))
                )
        ).forEach(doc -> mostPopularTagsHashMap.put(doc.getString("_id"), doc.getInteger("tagsNo")));
        return  mostPopularTagsHashMap;
    }


}