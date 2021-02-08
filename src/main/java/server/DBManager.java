package server;

import java.util.*;

public class DBManager {

    private DocumentDBManager documentDBMan;
    private GraphDBManager graphDBMan;

    public DBManager(){
        documentDBMan = new DocumentDBManager();
        //graphDBMan = new server.GraphDBManager();
    }

    public User[] findMostPopularTagsByLocation(String tag, String idUser){
    }

    public Map<User, Post> findMostAnsweredTopUserPosts(){
    }

    public User[] findTopExpertsByTag(String tag, String idUser){
    }

    public User[] getCorrelatedUsers(String username){

    }

    public Post getPostById(String postId){

    }

    public Post[] getPostsByTag(String[] tags){

    }

    public Post getPostByText(String text){

    }

    public User[] getRecommendedUsers(String username){

    }

    public User getUserData(String username){
        User user = documentDBMan.getUserData(username);

        return user;
    }

    public User[] getUsersRank(){

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

    }

    public boolean insertRelationPostsAnswer(String answerId, User user){

    }

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

    }

    public boolean removeRelationPostsAnswer(String userId, String answerId){

    }


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

    }
}
