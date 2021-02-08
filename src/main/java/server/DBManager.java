package server;

import java.util.*;

import client.*;
import middleware.*;

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
        documentDBMan.insertAnswer(answer, postId);
        graphDBMan.insertAnswer(answer);
        graphDBMan.insertRelationAnswerTo(answer.getAnswerId(), postId);

        return true;
    }

    public boolean insertFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){
        graphDBMan.insertFollowRelationAndUpdate(usernameFollower, usernameFollowed);

        return true;
    }

    public boolean insertPost(Post post){

        boolean res = documentDBMan.insertPost(post);

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

    public boolean insertRelationPostsQuestion(String postId, User user){

    }

    public boolean insertRelationVote(String answerId, String username){

    }

    public boolean removeAnswer(Answer answer){

    }

    public boolean removeFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){

    }

    public boolean removePost(Post post){

    }

    public boolean removeRelationAnswerTo(String postId, String answerId){

    }

    public boolean removeRelationContainsTag(String postId, String name){

    }

    public boolean removeRelationPostsAnswer(String userId, String answerId){

    }

    public boolean removeRelationPostsQuestion(String userId, String postId){

    }

    public boolean removeRelationVote(String userId, String answerId){

    }

    public boolean removeUser(User user){

    }

    public boolean updateUserData(User user){

    }
}
