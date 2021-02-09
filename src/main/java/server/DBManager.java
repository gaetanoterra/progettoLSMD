package server;

import java.util.*;

public class DBManager {

    private DocumentDBManager documentDBMan;
    private GraphDBManager graphDBMan;

    public DBManager(){
        documentDBMan = new DocumentDBManager();
        //graphDBMan = new server.GraphDBManager();
    }

    public String[] findMostPopularTagsByLocation(String location, int numTags){
        return documentDBMan.findMostPopularTagsByLocation(location, numTags);
    }

    public Map<User, Post> findMostAnsweredTopUserPosts(){
    }

    public User[] findTopExpertsByTag(String tag, int numExperts){
        return documentDBMan.findTopExpertsByTag(tag, numExperts);
    }

    //restituisco gli username degli utenti (nel graphDB ci sono solo quelli), poi quando seleziono uno specifico utente chiamo la getUserByUsername
    public String[] getCorrelatedUsers(String username){
        return graphDBMan.getCorrelatedUsers(username);
    }

    public Post[] getPostByDate(String data) { return documentDBMan.getPostByDate(data);}

    public Post getPostById(String postId){
        return documentDBMan.getPostById(postId);
    }

    public Post[] getPostByOwnerUsername(String username) { return documentDBMan.getPostByOwnerUsername(username); }

    public Post[] getPostsByTag(String[] tags){
        return documentDBMan.getPostsByTag(tags);
    }

    public Post[] getPostByText(String text){
        return documentDBMan.getPostsByText(text);
    }

    //restituisco gli username degli utenti (nel graphDB ci sono solo quelli), poi quando seleziono uno specifico utente chiamo la getUserByUsername
    public String[] getRecommendedUsers(String username, String tag){
        return graphDBMan.getRecommendedUsers(username, tag);
    }

    public User getUserData(String username){
        User user = documentDBMan.getUserData(username);

        return user;
    }

    public User[] getUsersRank(){
        return documentDBMan.getUsersRank();
    }

    public boolean insertAnswer(Answer answer, String postId){
        //inserisco la risposta el documentDB
        documentDBMan.insertAnswer(answer, postId);
        //inserisco il nodo Answer nel graphDB
        graphDBMan.insertAnswer(answer);
        //inserisco la relazione tra answer e post
        graphDBMan.insertRelationAnswerTo(answer.getAnswerId(), postId);
        //inserisco la relazione tra user e answer
        graphDBMan.insertRelationUserAnswer(answer.getAnswerId(), answer.getOwnerUserId());

        return true;
    }

    public boolean insertFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        graphDBMan.insertFollowRelationAndUpdate(usernameFollower, usernameFollowed);

        return true;
    }

    public boolean insertPost(Post post){

        boolean res = documentDBMan.insertPost(post);
        graphDBMan.insertRelationPostsQuestion(post.getPostId(), post.getOwnerUserId());
        graphDBMan.insertPost(post);

        return res;
    }

    public boolean insertUser(User user){

        boolean res = documentDBMan.insertUser(user);

        if(res)
            graphDBMan.insertUser(user);

        return res;
    }

    public boolean insertRelationContainsTag(String name, String postId){
        graphDBMan.insertRelationContainsTag(name, postId);

        return true;
    }

    /*public boolean insertRelationPostsAnswer(String answerId, User user){
    }*/

    public boolean insertRelationVote(String answerId, String userId, int voto){
        graphDBMan.insertRelationVote(answerId, userId, voto);

        return true;
    }

    public boolean removeAnswer(Answer answer, String postId){
        documentDBMan.removeAnswer(answer, postId);
        graphDBMan.removeAnswer(answer);
        graphDBMan.removeRelationAnswerTo(postId, answer.getAnswerId());
        graphDBMan.removeRelationUserAnswer(answer.getOwnerUserId(), answer.getAnswerId());

        return true;
    }

    public boolean removeFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        graphDBMan.removeFollowRelationAndUpdate(usernameFollower, usernameFollowed);

        return true;
    }

    public boolean removePost(Post post, String userId){
        boolean res = documentDBMan.removePost(post);
        graphDBMan.removePost(post);
        graphDBMan.removeRelationPostsQuestion(userId, post.getPostId());
        //devo eliminare tutte le risposte relative a questo post, aggiungere una query su graphDBManager (dato un post, eliminare tutte le risposte)

        return res;
    }

    public boolean removeRelationContainsTag(String postId, String name){
        graphDBMan.removeRelationContainsTag(postId, name);

        return true;
    }

    /*public boolean removeRelationPostsAnswer(String userId, String answerId){

    }*/


    public boolean removeRelationVote(String userId, String answerId){
        graphDBMan.removeRelationVote(userId, answerId);

        return true;
    }

    public boolean removeUser(User user){
        documentDBMan.removeUser(user.getUserId());
        graphDBMan.removeUser(user.getUserId());
        //rimuovere tutte le relazioni tra user e Answer
        //rimuovere tutte le relazioni tra user e Post
        //rimuovere tutti i Post dell'utente
        //rimuovere tutte le Answer dell'utente

        return true;
    }

    public boolean updateUserData(User user){
        return documentDBMan.updateUserData(user);
    }
}
