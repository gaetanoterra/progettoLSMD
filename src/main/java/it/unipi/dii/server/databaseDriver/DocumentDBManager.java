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

import java.time.Instant;
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
    db.Posts.createIndex({"Answers.Id": 1}, {unique: true, partialFilterExpression: {"Answers.Id": {$exists: true}}, name: "Answers.Id"})
    db.Posts.createIndex({Id: 1}, {unique: true, name: "Id"})
    db.Posts.createIndex({Body: "text"}, {name:"Body"})
    db.Users.createIndex({Id: 1}, {unique: true, name: "Id"})
        """;

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
        postsCollection = mongoDatabase.getCollection(POSTSCOLLECTION);
        usersCollection = mongoDatabase.getCollection(USERSCOLLECTION);

    }

    public void close(){
        this.mongoClient.close();
    }

    public String[] findMostPopularTagsByLocation(String location, int numTags){
        //trovo tutti gli utenti relativi ad una locazione
        //scorro tutti i post che hanno ownerUserId tra gli utenti trovati prima
        //raggruppo per tag e li conto
        ArrayList<String> tagList = new ArrayList<>();
        ArrayList<String> userIdList = new ArrayList<>();

        usersCollection.find(eq("Location", location)).forEach(document -> {
            userIdList.add(document.getInteger("Id").toString());
        });

        /*try (MongoCursor<Document> cursor = usersCollection.find(eq("Location", location)).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();
                User u = new User();
                //mi interessa solo lo userId
                u.setId(doc.getString("_id"));
                userList.add(u);
            }
        }*/

        //adesso che ho la lista di utenti scorro i post e trovo quelli che hanno ownerUserId tra i miei
        Bson matchStage = match(in("OwnerUserId", (userIdList.toArray(new String[userIdList.size()]))));
        Bson unwindStage = unwind("$Tags");
        //raggruppando su un attributo, questo dovrebbe diventare _id, e perde il nome originale
        Bson groupStage = group("$Tags", sum("totaleTags",1));
        Bson sortStage = sort(descending("totaleTags"));
        Bson limitStage = limit(numTags);

        postsCollection.aggregate(
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

        /*try (MongoCursor<Document> cursor = postsCollection.aggregate(Arrays.asList(matchStage, unwindStage, groupStage, sortStage, limitStage)).iterator())
        {
            while (cursor.hasNext())
            {
                Document doc = cursor.next();
                tagList.add(doc.getString("_id"));
            }
        }*/

        return ((tagList.toArray(new String[tagList.size()]).length == 0)? null : tagList.toArray(new String[tagList.size()]));
    }

    //restituisco gli id degli utenti più esperti
    public String[] findTopExpertsByTag(String tag, int num){
        ArrayList<String> userIdList = new ArrayList<>();

        Bson matchTag = match(eq("Tags", tag));
        Bson unwindAnswers = unwind("$Answers");
        //raggruppando su un attributo, questo dovrebbe diventare _id, e perde il nome originale
        Bson groupByOwnerUserId = group("$Answers.OwnerUserId", sum("totaleRisposteUtente",1));
        Bson sortByCountDesc = sort(descending("totaleRisposteUtente"));
        Bson limitStage = limit(num);

        //Bson projectStage = project(fields(include("$Answers.OwnerUserId")));

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


    public Post getPostById(String postId){

        List<Answer> answersList = new ArrayList<>();

        Document document = this.postsCollection.find(eq("GlobalPostId", postId)).first();

        if(document == null) {
            System.out.println("Found no document marching the id " + postId);
            return new Post();
        }else{
            this.increaseViewsPost(postId);

            document.getList("Answers", Document.class).forEach((answerDocument) -> {
                answersList.add(
                    new Answer(
                        answerDocument.getString("answerId"),
                        answerDocument.getLong("CreationDate"),
                        answerDocument.getInteger("Score"),
                        answerDocument.getString("OwnerUserId"),
                        answerDocument.getString("DisplayName"),
                        answerDocument.getString("Body"),
                        postId
                    )
                );
            });

            Post post = new Post(
                    document.getObjectId("_id").toString(),
                    postId,
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

    private void increaseViewsPost(String postId) {
        // Assuming the document already exists in the MongoDB collection
        this.postsCollection.updateOne(eq("_id", new ObjectId(postId)), inc("ViewCount", 1));
    }

    public ArrayList<Post> getPostByOwnerUsername(String username) {

        ArrayList<Post> posts = new ArrayList<>();
        postsCollection.find(all("DisplayName", username)).forEach(doc -> {
            /*
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
                    doc.getString("Title"),
                    answersList,
                    doc.getLong("CreationDate"),
                    doc.getString("Body"),
                    doc.getString("OwnerUserId"),
                    doc.getList("Tags", String.class)
            );*/
            Post p = new Post(
                    doc.getObjectId("_id").toString(),
                    doc.getString("GlobalPostId"),
                    doc.getString("Title"),
                    null,
                    null,
                    null,
                    null,
                    username,
                    null
            );

            posts.add(p);
        });

        System.out.println("getPostByOwnerUsername: found " + posts.size() + " posts matching the username " + username);
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

    private String generateTempIdAnswer(String postId, String userId) {
        //millisecondi da epoch + id del post + id dell'utente
        // - L
        return Instant.now().toEpochMilli() + postId + userId;
    }

    //TODO: rifare. il parentPostId deve essere il GlobalPostId di mongo
    public boolean insertAnswer(Answer answer){

      //  postsCollection.updateOne(eq("_id", new ObjectId(postId)), Updates.push("Answers", doc));
        // 3)
       // postsCollection.deleteOne(eq("TempId", tempId));

        return true;
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
            post.setMongoPost_id(result.getInsertedId().asObjectId().getValue().toString());
            // set post id as the objectId
            postsCollection.updateOne(eq("_id", result.getInsertedId().asObjectId().getValue()), set("Id", post.getMongoPost_id()));
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
            user.setUserId(result.getInsertedId().asObjectId().getValue().toString());
            // set user id as the objectId
            usersCollection.updateOne(eq("_id", result.getInsertedId().asObjectId().getValue()), set("Id", user.getUserId()));
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
                and(eq("Id", answer.getParentPostId()), eq("Answers.Id", answerId)),
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
        usersCollection.deleteOne(eq("DisplayName", displayName));
        return true;
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
        usersCollection.updateOne(eq("DisplayName", userIdFollower), inc("followerNumber", 1));
        usersCollection.updateOne(eq("DisplayName", userIdFollowed), inc("followedNumber", 1));
    }

    public void removeUserFollowerAndFollowedRelation(String userIdFollower, String userIdFollowed) {
        //TODO: Controllare se l'aggiornamento è corretto (differenza poco chiara tra followerNumber e followedNumber)
        usersCollection.updateOne(eq("DisplayName", userIdFollower), inc("followerNumber", -1));
        usersCollection.updateOne(eq("DisplayName", userIdFollowed), inc("followedNumber", -1));
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