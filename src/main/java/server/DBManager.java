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

    public User[] findMostPopularTagsByLocation(String tag, int idUser){
    }

    public Map<User, Post> findMostAnsweredTopUserPosts(){
    }

    public User[] findTopExpertsByTag(String tag, int idUser){
    }

    public User[] getCorrelatedUsers(String username){

    }

    public Post getPostById(int postId){

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

    public boolean insertAnswer(Answer answer){

    }

    public boolean insertFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){

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

    public boolean insertRelationAnswerTo(String postId, User user){

    }

    public boolean insertRelationContainsTag(String name, String postId){

    }

    public boolean insertRelationPostsAnswer(String answerId, User user){

    }

    public boolean insertRelationPostsQuestion(String postId, User user){

    }

    public boolean insertRelationVote(int postId,String username, int voto){

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

    public boolean removeUser(String username){

    }

    public boolean updateUserData(User user){

    }
}
