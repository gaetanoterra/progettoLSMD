package it.unipi.dii.server.databaseDriver;

import com.mongodb.client.model.*;
import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.BasicDBObject;
import org.bson.types.ObjectId;

//TODO: Necessaria revisione dei metodi per verificare se sono stati implementati nella loro completezza
public class DocumentDBManager {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private final String POSTSCOLLECTION = "Posts";
    private final String USERSCOLLECTION = "Users";
    private final String IdMetadata = "MetadataDocument";
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


    // query cambio tipo fields
    private final String queries = """
    db.Posts.updateMany(
    {},
            [
    {
        $set: {
            Answers: {
                $map: { input: "$Answers", in: { $mergeObjects: [ "$$this", { OwnerUserId: { $toInt: "$$this.OwnerUserId" } } ] } }
            }
        }
    }
] )

        db.Posts.find().forEach(function(doc) {
        if (doc.OwnerUserId == null) {
            return;
        }
        db.Posts.updateOne(
                { "_id": doc._id},
        {$set:
        { "OwnerUserId": new NumberInt(doc.OwnerUserId) }
        }
	);
    });

db.Posts.find().forEach(function(doc) {
        if (doc.ViewCount == null) {
            return;
        }
        db.Posts.updateOne(
                { "_id": doc._id},
        {$set:
        { "ViewCount": new NumberLong(doc.ViewCount) }
        }
	);
    });
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
        initializeMetadata();
        System.out.println("Metadati MongoDB inizializzati");
    }

    private Integer getLastPostId() {
        return postsCollection.find(new Document("_id", IdMetadata)).first().getInteger(IdValueMetadata, 1);
    }

    private Integer getLastUserId() {
        return usersCollection.find(new Document("_id", IdMetadata)).first().getInteger(IdValueMetadata, 1);
    }

    private void initializeMetadata() {
        //entrambi i documenti hanno questa forma
        // {_id: "metadata", lastId: Long}
        // esempio
        // {_id: "metadata", lastId: 211104}
        // ultimo id tra i post
        Integer lastPostId = postsCollection.aggregate(
                Arrays.asList(
                        new Document("$project",
                                new Document("_id", 0L)
                                        .append("Id", 1L)
                                        .append("MaxAnswerId",
                                                new Document("$max", "$Answers.Id")
                                        )
                        ),
                        new Document("$project",
                                new Document("MaxPostId",
                                        new Document("$max",
                                                Arrays.asList("$Id", "$MaxAnswerId")
                                        )
                                )
                        ),
                        new Document("$group",
                                new Document("_id", "1")
                                        .append("MaxPostId",
                                                new Document("$max", "$MaxPostId")
                                        )
                        )
                )
        ).first().getInteger("MaxPostId", 1);
        postsCollection.findOneAndUpdate(
                new Document("_id", IdMetadata),
                set(IdValueMetadata, lastPostId),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(true)
        );
        // ultimo id tra gli utenti
        Integer lastUserId = usersCollection.aggregate(
                Arrays.asList(
                        new Document("$project",
                                new Document("_id", 0L)
                                        .append("Id", 1L)),
                        new Document("$group",
                                new Document("_id", "1")
                                        .append("MaxUserId",
                                                new Document("$max", "$Id")
                                        )
                        )
                )
        ).first().getInteger("MaxUserId", 1);
        usersCollection.findOneAndUpdate(
                new Document("_id", IdMetadata),
                set(IdValueMetadata, lastUserId),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(true)
        );
    }

    public Integer getNewPostId() {
        Document document = postsCollection.findOneAndUpdate(
                new Document("_id", IdMetadata),
                inc(IdValueMetadata, 1),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        );
        return document.getInteger(IdValueMetadata);
    }

    public Integer getNewUserId() {
        Document document = usersCollection.findOneAndUpdate(
                new Document("_id", IdMetadata),
                inc(IdValueMetadata, 1),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        );
        return document.getInteger(IdValueMetadata);
    }

    public void close(){
        this.mongoClient.close();
    }

    public Map<User, Post[]> findMostAnsweredTopUserPosts(){
/* QUESTA VA FATTA CON NEO4J
        //Find 50 most followed users, and for each of them show the 3 posts they wrote that contains the largest number of answers

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
        usersCollection.aggregate(
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
        postsCollection.aggregate(
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
        ArrayList<String> tagList = new ArrayList<>();
        ArrayList<Integer> userIdList = new ArrayList<>();

        usersCollection.find(eq("Location", location)).forEach(document -> {
            userIdList.add(document.getInteger("Id"));
        });

        /*try (MongoCursor<Document> cursor = usersCollection.find(eq("Location", location)).iterator())
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

        /*try (MongoCursor<Document> cursor = postsCollection.aggregate(Arrays.asList(m, u, g, s, l)).iterator())
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
        ArrayList<String> userIdList = new ArrayList<>();
        ArrayList<User> userList = new ArrayList<>();

        Bson matchTag = match(eq("Tags", tag));
        Bson unwindAnswers = unwind("Answers");
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

        usersCollection.find(in("Id", (String[])userIdList.toArray())).forEach(document -> {
            User user = new User()
                    .setUserId(document.getInteger("Id"))
                    .setDisplayName(document.getString("DisplayName"))
                    .setPassword(document.getString("Password"))
                    .setFollowersNumber(document.getInteger("followerNumber"))
                    .setFollowedNumber(document.getInteger("followedNumber"))
                    .setReputation(document.getInteger("Reputation"))
                    .setCreationDate(document.getLong("CreationDate"))
                    .setLastAccessDate(document.getLong("LastAccessDate"))
                    .setType(document.getString("type"))
                    .setLocation(document.getString("Location"))
                    .setAboutMe(document.getString("AboutMe"))
                    .setWebsiteURL(document.getString("WebsiteUrl"));
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
        usersCollection.aggregate(
                Arrays.asList(
                        sortByFollowersDesc,
                        limitUsers
                )
        ).forEach(document -> {
            User user = new User()
                    .setUserId(document.getInteger("Id"))
                    .setDisplayName(document.getString("DisplayName"))
                    .setPassword(document.getString("Password"))
                    .setFollowersNumber(document.getInteger("followerNumber"))
                    .setFollowedNumber(document.getInteger("followedNumber"))
                    .setReputation(document.getInteger("Reputation"))
                    .setCreationDate(document.getLong("CreationDate"))
                    .setLastAccessDate(document.getLong("LastAccessDate"))
                    .setType(document.getString("type"))
                    .setLocation(document.getString("Location"))
                    .setAboutMe(document.getString("AboutMe"))
                    .setWebsiteURL(document.getString("WebsiteUrl"));
            // user done, now the three posts
            // for each user id ($Id)
            Integer userId = user.getUserId();
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
            postsCollection.aggregate(Arrays.asList(matchOwnerUserId, unwindAnswers, unwindTags, groupByTag, sortByCountDesc, limitTags, projectTagCount)).forEach(doc ->
                    list.add(new Pair<>(doc.getString("tag"), doc.getInteger("count")))
            );
            result.put(user, (Pair<String, Integer>[]) list.toArray());
        });
        return result;
    }

    public ArrayList<Post> getPostByDate(String data) {

        ArrayList<Post> posts = new ArrayList<>();
        postsCollection.find(eq("CreationDate", data)).forEach(doc -> {
            Post p = new Post(doc.getInteger("PostId"),
                    doc.getString("Title"),
                    (ArrayList<Answer>)doc.get("Answers"),
                    doc.getDate("CreationDate"),
                    doc.getString("Body"),
                    doc.getInteger("OwnerUserId"),
                    (ArrayList<String>)doc.get("Tags"));
            posts.add(p);
        });
        System.out.println("found " + posts.size() + " posts matching the date given as input");
        return posts;
    }

    public Post getPostById(Integer postId){

        ArrayList<Answer> answersList = new ArrayList<>();

        Document document = this.postsCollection.find(
                                        eq("Id", postId)).first();

        if(document == null) {
            System.out.println("Found no document marching the id " + postId);
            return new Post();
        }else{
            this.increaseViewsPost(postId);
            System.out.println("Found one document marching the id " + postId + " Containing " +
                                document.getList("Answers", Document.class).size() + " answers");

            document.getList("Answers", Document.class).forEach((answerDocument) -> {
                answersList.add(
                    new Answer(
                        answerDocument.getInteger("Id"),
                        new Date(answerDocument.getLong("CreationDate")),
                        answerDocument.getInteger("Score"),
                        answerDocument.getString("OwnerDisplayName"),
                        answerDocument.getString("Body")
                    )
                );
            });

            Post post = new Post(
                    postId,
                    document.getString("Title"),
                    answersList,
                    new Date(document.getLong("CreationDate")),
                    document.getString("Body"),
                    document.getInteger("OwnerUserId"),
                    document.getList("Tags", String.class)
            );
            post.setOwnerUserName(document.getString("OwnerDisplayName"));
            return post;
        }

    }

    private void increaseViewsPost(Integer postId) {
        // Assuming the document already exists in the MongoDB collection
        this.postsCollection.updateOne(eq("Id", postId), inc("ViewCount", 1));
    }

    public ArrayList<Post> getPostByOwnerUsername(String username) {

        ArrayList<Post> posts = new ArrayList<>();
        postsCollection.find(all("OwnerUserId", username)).forEach(doc -> {
            Post p = new Post(doc.getInteger("PostId"),
                    doc.getString("Title"),
                    (ArrayList<Answer>)doc.get("Answers"),
                    new Date(doc.getLong("CreationDate")),
                    doc.getString("Body"),
                    doc.getInteger("OwnerUserId"),
                    (ArrayList<String>)doc.get("Tags"));

            posts.add(p);
        });

        System.out.println("found " + posts.size() + " posts matching the username given as input");
        return posts;
    }

    public ArrayList<Post> getPostsByTag(String[] tags){

        ArrayList<Post> postArrayList = new ArrayList<>();
        postsCollection.find(all("Tags", tags)).forEach(doc -> {
            Post p = new Post(doc.getInteger("PostId"),
                    doc.getString("Title"),
                    (ArrayList<Answer>)doc.get("Answers"),
                    doc.getDate("CreationDate"),
                    doc.getString("Body"),
                    doc.getInteger("OwnerUserId"),
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
                                    .append("Id", 1)
                                    .append("ViewCount", 1)
                                    .append("OwnerUserId", 1)
                                    .append("Tags" , 1)
                                    .append("AnswersNumber",new BasicDBObject("$size","$Answers"))
                        )
                        .forEach(doc -> {
                            Post p = new Post(doc.getInteger("Id"),
                                              doc.getString("Title"),
                                              doc.getInteger("AnswersNumber"),
                                              doc.getInteger("OwnerUserId"),
                                              doc.getList("Tags", String.class));
                            p.setViews(doc.getLong("ViewCount"));
                            postArrayList.add(p);
                        }
        );

        System.out.println("found " + postArrayList.size() + " posts matching the text given as input");
        return postArrayList;
    }

    public User getUserById(Integer userId) {

        Document userDoc = usersCollection.find(eq("Id", userId)).first();
        User user = new User();

        if(userDoc != null) {
            user.setUserId(userId)
                .setDisplayName(userDoc.getString("DisplayName"))
                .setPassword(userDoc.getString("Password"))
                .setFollowersNumber(userDoc.getInteger("followerNumber"))
                .setFollowedNumber(userDoc.getInteger("followedNumber"))
                .setReputation(userDoc.getInteger("Reputation"))
                .setCreationDate(userDoc.getLong("CreationDate"))
                .setLastAccessDate(userDoc.getLong("LastAccessDate"))
                .setType(userDoc.getString("type"))
                .setLocation(userDoc.getString("Location"))
                .setAboutMe(userDoc.getString("AboutMe"))
                .setWebsiteURL(userDoc.getString("WebsiteUrl"));
        }

        return user;
    }

    public User getUserData(String displayName){

        Document userDoc = usersCollection.find(eq("DisplayName", displayName)).first();
        User user = new User();

        if(userDoc != null) {
            user.setUserId(userDoc.getInteger("Id"))
                    .setDisplayName(displayName)
                    .setPassword(userDoc.getString("Password"))
                    .setFollowersNumber(userDoc.getInteger("followerNumber"))
                    .setFollowedNumber(userDoc.getInteger("followedNumber"))
                    .setReputation(userDoc.getInteger("Reputation"))
                    .setCreationDate(userDoc.getLong("CreationDate"))
                    .setLastAccessDate(userDoc.getLong("LastAccessDate"))
                    .setType(userDoc.getString("type"))
                    .setLocation(userDoc.getString("Location"))
                    .setAboutMe(userDoc.getString("AboutMe"))
                    .setWebsiteURL(userDoc.getString("WebsiteUrl"));

            System.out.println(User.convertMillisToDate(user.getLastAccessDate()));
        }

        return user;
    }

    public User[] getUsersRank(){

        ArrayList<User> user = new ArrayList<>();
        usersCollection.find().sort(descending("Reputation")).limit(10).forEach(document -> {
            User u = new User()
                    .setUserId(document.getInteger("Id"))
                    .setDisplayName(document.getString("DisplayName"))
                    .setPassword(document.getString("Password"))
                    .setFollowersNumber(document.getInteger("followerNumber"))
                    .setFollowedNumber(document.getInteger("followedNumber"))
                    .setReputation(document.getInteger("Reputation"))
                    .setCreationDate(document.getLong("CreationDate"))
                    .setLastAccessDate(document.getLong("LastAccessDate"))
                    .setType(document.getString("type"))
                    .setLocation(document.getString("Location"))
                    .setAboutMe(document.getString("AboutMe"))
                    .setWebsiteURL(document.getString("WebsiteUrl"));

            user.add(u);
        });

        return (User[]) user.toArray();
    }

    public boolean insertAnswer(Answer answer, String postId){

        Document doc = new Document("AnswerId", answer.getAnswerId())
                .append("CreationDate", answer.getCreationDate())
                .append("Score", answer.getScore())
                .append("OwnerUserId", answer.getOwnerUserName());

        postsCollection.updateOne(eq("PostId", postId), Updates.push("Answers", doc));

        return true;
    }

    public boolean insertPost(Post post){

        Document doc = new Document("PostId", post.getPostId())
                .append("Title", post.getTitle())
                .append("Answers", post.getAnswers())
                .append("CreationDate", post.getCreationDate())
                .append("Body", post.getBody())
                .append("OwnerUserId", post.getOwnerUserId())
                .append("Tags", post.getTags());

        postsCollection.insertOne(doc);

        return true;
    }

    public boolean insertUser(User user){

        Document us = new Document("Id", user.getUserId())
                .append("DisplayName", user.getDisplayName())
                .append("Password", user.getPassword())
                .append("CreationDate", user.getCreationDate())
                .append("LastAccessDate", user.getLastAccessDate())
                .append("Location", user.getLocation())
                .append("AboutMe", user.getAboutMe())
                .append("WebsiteUrl", user.getWebsiteURL())
                .append("followedNumber", user.getFollowedNumber())
                .append("followerNumber", user.getFollowersNumber())
                .append("Reputation", user.getReputation())
                .append("type", user.getType());
        return usersCollection.insertOne(us).wasAcknowledged();
    }

    //
    private boolean checkUser(String displayName) {
        boolean res = false;

        long count = usersCollection.countDocuments(eq("DisplayName", displayName));

        if(count > 0)
            res = true;

        return res;
    }

    public boolean removeAnswer(Answer answer, String postId){

        /*Document doc = new Document("AnswerId", answer.getAnswerId()).append("CreationDate", answer.getCreationDate()).append("Score", answer.getScore()).append("OwnerUserId", answer.getOwnerUserId());
        postsCollection.updateOne(eq("PostId", postId), Updates.pull("Answers", doc));*/

        //provare uno dei due
        BasicDBObject match = new BasicDBObject("PostId", postId);
        BasicDBObject update = new BasicDBObject("Answers", new BasicDBObject("AnswerId", answer.getAnswerId()));
        postsCollection.updateOne(match, new BasicDBObject("$pull", update));

        return true;
    }

    public boolean removePost(Post post){
        postsCollection.deleteOne(eq("PostId", post.getPostId()));
        return true;
    }

    public boolean removeUser(Integer userIdString){
        //addio utente
        usersCollection.deleteOne(eq("Id", userIdString));
        //addio post scritti da lui
        return true;
    }

    public boolean updateUserData(User user){
        usersCollection.updateOne(
                eq("Id", user.getUserId()),
                Updates.combine(
                        set("Password", user.getPassword()),
                        set("Location", user.getLocation()),
                        set("AboutMe", user.getAboutMe()),
                        set("WebsiteUrl", user.getWebsiteURL())
                )
        );

        return true;
    }

    public void insertUserFollowerAndFollowedRelation(Integer userIdFollower, Integer userIdFollowed) {
        //TODO: Controllare se l'aggiornamento è corretto (differenza poco chiara tra followerNumber e followedNumber)
        usersCollection.updateOne(eq("Id", userIdFollower), inc("followerNumber", 1));
        usersCollection.updateOne(eq("Id", userIdFollowed), inc("followedNumber", 1));
    }

    public void removeUserFollowerAndFollowedRelation(Integer userIdFollower, Integer userIdFollowed) {
        //TODO: Controllare se l'aggiornamento è corretto (differenza poco chiara tra followerNumber e followedNumber)
        usersCollection.updateOne(eq("Id", userIdFollower), inc("followerNumber", -1));
        usersCollection.updateOne(eq("Id", userIdFollowed), inc("followedNumber", -1));
    }
}