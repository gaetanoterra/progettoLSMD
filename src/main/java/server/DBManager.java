package server;

import java.util.*;

//classe preposta a ricevere le richieste dal clientManager e a propagarle al documentDB e al graphDB
public class DBManager {

    private DocumentDBManager documentDBManager;
    private GraphDBManager graphDBManager;

    public DBManager(){
        documentDBManager = new DocumentDBManager();
        graphDBManager = new GraphDBManager(
                "bolt://host-1:7687",
                "neo4j",
                "pseudostackoverdb"
        );
    }
    public void close(){
        this.documentDBManager.close();
        this.graphDBManager.close();
    }

    public String[] findMostPopularTagsByLocation(String location, int numTags){
        return documentDBManager.findMostPopularTagsByLocation(location, numTags);
    }

    public Map<User, Post> findMostAnsweredTopUserPosts(){
        return this.graphDBManager.findMostAnsweredTopUserPosts();
    }

    public User[] findTopExpertsByTag(String tag, int numExperts){
        return documentDBManager.findTopExpertsByTag(tag, numExperts);
    }

    //restituisco gli username degli utenti (nel graphDB ci sono solo quelli), poi quando seleziono uno specifico utente chiamo la getUserByUsername
    public String[] getCorrelatedUsers(String username){
        return graphDBManager.getCorrelatedUsers(username);
    }

    public Post[] getPostByDate(String postCreationDate) {
        return documentDBManager.getPostByDate(postCreationDate);
    }

    public Post getPostById(String postId){
        return documentDBManager.getPostById(postId);
    }

    public Post[] getPostByOwnerUsername(String ownerPostUsername) {
        return documentDBManager.getPostByOwnerUsername(ownerPostUsername);
    }

    public Post[] getPostsByTag(String[] tags){
        return documentDBManager.getPostsByTag(tags);
    }

    public Post[] getPostByText(String text){
        return documentDBManager.getPostsByText(text);
    }

    //restituisco gli username degli utenti (nel graphDB ci sono solo quelli), poi quando seleziono uno specifico utente chiamo la getUserByUsername
    public String[] getRecommendedUsers(String username, String tag){
        return graphDBManager.getRecommendedUsers(username, tag);
    }

    public User getUserData(String username){
        return documentDBManager.getUserData(username);
    }

    public User[] getUsersRank(){
        return documentDBManager.getUsersRank();
    }

    public boolean insertAnswer(Answer answer, String postId){
        //inserisco la risposta el documentDB
        documentDBManager.insertAnswer(answer, postId);
        //inserisco il nodo Answer nel graphDB
        graphDBManager.insertAnswer(answer);
        //inserisco la relazione tra answer e post
        graphDBManager.insertRelationAnswerTo(answer.getAnswerId(), postId);
        //inserisco la relazione tra user e answer
        graphDBManager.insertRelationUserAnswer(answer.getAnswerId(), answer.getOwnerUserId());

        return true;
    }

    public boolean insertFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        graphDBManager.insertFollowRelationAndUpdate(usernameFollower, usernameFollowed);

        return true;
    }

    public boolean insertPost(Post newPost){

        boolean insertedPost = documentDBManager.insertPost(newPost);
        graphDBManager.insertRelationPostsQuestion(newPost.getPostId(), newPost.getOwnerUserId());
        graphDBManager.insertPost(newPost);

        return insertedPost;
    }

    public boolean insertUser(User newUser){

        boolean insertedUser = documentDBManager.insertUser(newUser);

        if(insertedUser)
            graphDBManager.insertUser(newUser);

        return insertedUser;
    }

    public boolean insertRelationContainsTag(String name, String postId){
        graphDBManager.insertRelationContainsTag(name, postId);
        return true;
    }

    //TODO: Definire il metodo insertRelationPostsAnswer (e possibilmente invertire ordine argomenti)
    /*
    public boolean insertRelationPostsAnswer(String answerId, User user){
    }
    */

    public boolean insertRelationVote(String answerId, String userId, int voto){
        graphDBManager.insertRelationVote(answerId, userId, voto);
        return true;
    }

    public boolean removeAnswer(Answer answer, String postId){
        documentDBManager.removeAnswer(answer, postId);
        graphDBManager.removeAnswer(answer);
        graphDBManager.removeRelationAnswerTo(postId, answer.getAnswerId());
        graphDBManager.removeRelationUserAnswer(answer.getOwnerUserId(), answer.getAnswerId());

        return true;
    }

    public boolean removeFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        graphDBManager.removeFollowRelationAndUpdate(usernameFollower, usernameFollowed);

        return true;
    }

    public boolean removePost(Post post, String userId){
        boolean postRemoved = documentDBManager.removePost(post);
        graphDBManager.removePost(post);
        graphDBManager.removeRelationPostsQuestion(userId, post.getPostId());
        //devo eliminare tutte le risposte relative a questo post, aggiungere una query su graphDBManager (dato un post, eliminare tutte le risposte)

        return postRemoved;
    }

    public boolean removeRelationContainsTag(String postId, String name){
        graphDBManager.removeRelationContainsTag(postId, name);

        return true;
    }

    //TODO: Definire il metodo removeRelationPostsAnswer
    /*public boolean removeRelationPostsAnswer(String userId, String answerId){

    }*/


    public boolean removeRelationVote(String userId, String answerId){
        graphDBManager.removeRelationVote(userId, answerId);

        return true;
    }

    //TODO: Definire il metodo removeUser nella sua completezza (relazioni, post, e answer dell'utente)
    public boolean removeUser(User user){
        documentDBManager.removeUser(user.getUserId());
        graphDBManager.removeUser(user.getUserId());
        //rimuovere tutte le relazioni tra user e Answer
        //rimuovere tutte le relazioni tra user e Post
        //rimuovere tutti i Post dell'utente
        //rimuovere tutte le Answer dell'utente

        return true;
    }

    public boolean updateUserData(User user){
        return documentDBManager.updateUserData(user);
    }
}
