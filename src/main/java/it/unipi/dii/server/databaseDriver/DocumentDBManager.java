package it.unipi.dii.server.databaseDriver;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertOneResult;
import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import javafx.util.Pair;
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
                
            db.Posts.find().forEach(function(doc) {
                    if (doc.Id == null) {
                        return;
                    }
                    db.Posts.updateOne(
                            { "_id": doc._id},
                    {$set:
                    { "Id": doc.Id.toString() }
                    }
            	);
            });
            
                db.Posts.aggregate([ {"$addFields": {AccountId : {$toString: "$AccountId"}}}]);
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
        // initializeMetadata();
        // System.out.println("Metadati MongoDB inizializzati");
    }
    //TODO: Da rimuovere se necessario
    private Integer getLastPostId() {
        return postsCollection.find(new Document("_id", IdMetadata)).first().getInteger(IdValueMetadata, 1);
    }
    //TODO: Da rimuovere se necessario
    private Integer getLastUserId() {
        return usersCollection.find(new Document("_id", IdMetadata)).first().getInteger(IdValueMetadata, 1);
    }
    //TODO: Da rimuovere se necessario
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
    //TODO: Da rimuovere se necessario
    private Integer getNewPostId() {
        Document document = postsCollection.findOneAndUpdate(
                new Document("_id", IdMetadata),
                inc(IdValueMetadata, 1),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        );
        return document.getInteger(IdValueMetadata);
    }
    //TODO: Da rimuovere se necessario
    private Integer getNewUserId() {
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
                                "$_id",
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
                userIdList.add(doc.getString("_id"))
        );

        // adesso devo trovare i loro post
        Map<User, Post[]> utentiSeguitiPost = new HashMap<>();

        Bson matchStage = match(in("OwnerUserId", userIdList));
        Bson projectStage2 = new Document(
                "$project",
                new Document(
                        "OwnerUserId", 1
                ).append(
                        "_id", 1
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
        ArrayList<User> userList = new ArrayList<>();

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
                userIdList.add(doc.getString("Answer.OwnerDisplayName"))
        );

        /*usersCollection.find(in("Id", userIdList.toArray(new String[userIdList.size()]))).forEach(document -> {
            User user = new User()
                    .setUserId(document.getObjectId("_id").toString())
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
        });*/

        return ((userIdList.toArray(new String[userIdList.size()]).length == 0)? null : userIdList.toArray(new String[userIdList.size()]));
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
        Bson limitUsers = limit(10);
        usersCollection.aggregate(
                Arrays.asList(
                        sortByFollowersDesc,
                        limitUsers
                )
        ).forEach(document -> {
            User user = new User()
                    .setUserId(Integer.toString(document.getInteger("Id")))
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
            String userId = user.getUserId();
            Bson matchOwnerUserId = match(eq("Answers.OwnerUserId", userId)); //ownerUserId è di tipo String
            Bson unwindAnswers = unwind("$Answers");
            Bson unwindTags = unwind("$Tags");
            Bson groupByTag = group("$Tags", sum("count", 1));
            Bson sortByCountDesc = sort(descending("count"));
            Bson limitTags = limit(3);
            Bson projectTagCount = project(fields(include("_id", "count")));
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
                    list.add(new Pair<>(doc.getString("_id"), doc.getInteger("count")))
            );
            result.put(user, list.toArray(new Pair[list.size()]));
        });
        return result;
    }

    public ArrayList<Post> getPostByDate(String data) {

        ArrayList<Post> posts = new ArrayList<>();
        postsCollection.find(eq("CreationDate", data)).forEach(doc -> {
            Post p = new Post(
                    doc.getObjectId("_id").toString(),
                    doc.getString("Title"),
                    doc.getList("Answers", Answer.class),
                    doc.getLong("CreationDate"),
                    doc.getString("Body"),
                    doc.getString("OwnerUserId"),
                    doc.getList("Tags", String.class)
            );
            posts.add(p);
        });
        System.out.println("found " + posts.size() + " posts matching the date given as input");
        return posts;
    }

    public Post getPostById(String postId){

        ArrayList<Answer> answersList = new ArrayList<>();

        Document document = this.postsCollection.find(
                                        eq("_id", new ObjectId(postId))).first();

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
                        answerDocument.getString("Id"),
                        answerDocument.getLong("CreationDate"),
                        answerDocument.getInteger("Score"),
                        answerDocument.getString("OwnerUserId"),
                        answerDocument.getString("OwnerDisplayName"),
                        answerDocument.getString("Body"),
                        postId
                    )
                );
            });

            Post post = new Post(
                    postId,
                    document.getString("Title"),
                    answersList,
                    document.getLong("CreationDate"),
                    document.getString("Body"),
                    document.getString("OwnerUserId"),
                    document.getList("Tags", String.class)
            );
            post.setOwnerUserName(document.getString("OwnerDisplayName"));
            return post;
        }

    }

    private void increaseViewsPost(String postId) {
        // Assuming the document already exists in the MongoDB collection
        this.postsCollection.updateOne(eq("_id", new ObjectId(postId)), inc("ViewCount", 1));
    }

    public ArrayList<Post> getPostByOwnerUsername(String username) {

        ArrayList<Post> posts = new ArrayList<>();
        postsCollection.find(all("OwnerUserId", username)).forEach(doc -> {
            Post p = new Post(
                    doc.getObjectId("_id").toString(),
                    doc.getString("Title"),
                    doc.getList("Answers", Answer.class),
                    doc.getLong("CreationDate"),
                    doc.getString("Body"),
                    doc.getString("OwnerUserId"),
                    doc.getList("Tags", String.class)
            );
            posts.add(p);
        });

        System.out.println("found " + posts.size() + " posts matching the username given as input");
        return posts;
    }

    public ArrayList<Post> getPostsByTag(String[] tags){

        ArrayList<Post> postArrayList = new ArrayList<>();
        postsCollection.find(all("Tags", tags)).forEach(doc -> {
            Post p = new Post(
                    doc.getObjectId("_id").toString(),
                    doc.getString("Title"),
                    doc.getList("Answers", Answer.class),
                    doc.getLong("CreationDate"),
                    doc.getString("Body"),
                    doc.getString("OwnerUserId"),
                    doc.getList("Tags", String.class)
            );

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
                                    .append("_id", 1)
                                    .append("ViewCount", 1)
                                    .append("OwnerUserId", 1)
                                    .append("Tags" , 1)
                                    .append("AnswersNumber",new BasicDBObject("$size","$Answers"))
                        )
                        .forEach(doc -> {
                            Post p = new Post(doc.getObjectId("_id").toString(),
                                              doc.getString("Title"),
                                              doc.getInteger("AnswersNumber"),
                                              doc.getString("OwnerUserId"),
                                              doc.getList("Tags", String.class));
                            p.setViews(Long.parseLong(doc.getString("ViewCount")));
                            postArrayList.add(p);
                        }
        );

        System.out.println("found " + postArrayList.size() + " posts matching the text given as input");
        return postArrayList;
    }

    public User getUserById(String userId) {

        Document userDoc = usersCollection.find(eq("_id", new ObjectId(userId))).first();
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
            user.setUserId(userDoc.getObjectId("_id").toString())
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

            //System.out.println(User.convertMillisToDate(user.getLastAccessDate()));
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
                    .setType(document.getString("type"))
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

    public boolean insertAnswer(Answer answer, String postId){
        // strategia: visto che una risposta deve avere un id (per lato neo4j), e che usare l'indice
        // della posizione nell'array come id può essere difficile da usare (visto che per usare come id l'indice
        // devo prima trovare il documento nell'array e perché usare l'indice vuol dire che in neo4j bisogna usare
        // la tripletta utente-indice-post per identificare una risposta), ho deciso di proseguire cosi'.
        // 1) Creare un documento temporaneo, così da ottenere l'objectId associato
        // 2) Usare l'objectId come nuovo id della risposta, e inserire il documento
        // 3) Rimuovere il documento temporaneo
        // - L

        // 1)
        String tempId = generateTempIdAnswer(postId, answer.getOwnerUserId());
        Document doc = new Document("TempId", tempId);
        InsertOneResult result = postsCollection.insertOne(doc);
        // 2)
        answer.setAnswerId(result.getInsertedId().asObjectId().getValue().toString());
        doc = new Document("Id", answer.getAnswerId())
                .append("CreationDate", answer.getCreationDate())
                .append("Score", answer.getScore())
                .append("OwnerUserId", answer.getOwnerUserId())
                .append("Body", answer.getBody())
                .append("OwnerDisplayName", answer.getOwnerUserName());
        postsCollection.updateOne(eq("_id", new ObjectId(postId)), Updates.push("Answers", doc));
        // 3)
        postsCollection.deleteOne(eq("TempId", tempId));

        return true;
    }

    public boolean insertPost(Post post){

        Document doc = new Document()
                .append("Title", post.getTitle())
                .append("Answers", post.getAnswers())
                .append("CreationDate", post.getCreationDate())
                .append("Body", post.getBody())
                .append("OwnerUserId", post.getOwnerUserId())
                .append("OwnerDisplayName", post.getOwnerUserName())
                .append("Tags", post.getTags())
                .append("ViewCount", post.getViews());

        InsertOneResult result = postsCollection.insertOne(doc);
        post.setPostId(result.getInsertedId().asObjectId().getValue().toString());
        return result.wasAcknowledged();
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
                .append("type", user.getType());

        InsertOneResult result = usersCollection.insertOne(userDoc);
        user.setUserId(result.getInsertedId().asObjectId().getValue().toString());
        return result.wasAcknowledged();
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

        /*Document doc = new Document("_id", answer.getAnswerId()).append("CreationDate", answer.getCreationDate()).append("Score", answer.getScore()).append("OwnerUserId", answer.getOwnerUserId());
        postsCollection.updateOne(eq("_id", postId), Updates.pull("Answers", doc));*/

        //provare uno dei due
        BasicDBObject match = new BasicDBObject("_id", new ObjectId(postId));
        BasicDBObject update = new BasicDBObject("Answers", new BasicDBObject("Id", answer.getAnswerId()));
        postsCollection.updateOne(match, new BasicDBObject("$pull", update));

        return true;
    }

    public boolean removePost(Post post){
        postsCollection.deleteOne(eq("_id", new ObjectId(post.getPostId())));
        return true;
    }

    public boolean removeUser(String userId){
        //addio utente
        usersCollection.deleteOne(eq("_id", new ObjectId(userId)));
        //addio post scritti da lui
        //TODO: Rimuovere tutti i post dell'utente
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

    public void updateVotesAnswer(String postId, String answerId, int vote) {
        postsCollection.updateOne(
                and(eq("_id", new ObjectId(postId)), eq("Answers.Id", answerId)),
                Updates.inc("Answers.$.Score", vote)
        );
    }


    public void insertUserFollowerAndFollowedRelation(String userIdFollower, String userIdFollowed) {
        //TODO: Controllare se l'aggiornamento è corretto (differenza poco chiara tra followerNumber e followedNumber)
        usersCollection.updateOne(eq("_id", new ObjectId(userIdFollower)), inc("followerNumber", 1));
        usersCollection.updateOne(eq("_id", new ObjectId(userIdFollowed)), inc("followedNumber", 1));
    }

    public void removeUserFollowerAndFollowedRelation(String userIdFollower, String userIdFollowed) {
        //TODO: Controllare se l'aggiornamento è corretto (differenza poco chiara tra followerNumber e followedNumber)
        usersCollection.updateOne(eq("_id", new ObjectId(userIdFollower)), inc("followerNumber", -1));
        usersCollection.updateOne(eq("_id", new ObjectId(userIdFollowed)), inc("followedNumber", -1));
    }

    //TODO: possibili analytics
    //TODO
    //TODO
    //TODO
    //TODO
    //trovare la location più social, cioè trovare le location dove gli utenti hanno più followers e followed

    //trovare possibili troll, cioè utenti che hanno tante risposte (rispondono tanto) ma che hanno poche risposte accettate
    /* quindi devo trovare per ogni utente tutte le sue risposte e per ogni utente tutte le risposte accettate date da lui
    * perciò in una prima query faccio un unwind sulle Answers e raggruppo sull'Answers.OwnerUserId sommando le risposte => ho il numero totale di risposte per ogni utenre
    * in un'altra query per ogni utente trovato sopra*/
}