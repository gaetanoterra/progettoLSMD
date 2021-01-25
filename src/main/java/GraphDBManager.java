public class GraphDBManager {

    private Driver dbConnection;

    public Map<User, Post> findMostAnsweredTopUserPosts(){

    }

    public User[] getCorrelatedUsers(String username){

    }

    public User[] getRecommendedUsers(String username){

    }

    public boolean insertAnswer(Answer answer){

    }

    public boolean insertFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){

    }

    public boolean insertPost(Post post){

    }

    public boolean insertUser(User user){

    }

    public boolean insertVote(int postId,String username, int voto){

    }

    public boolean removeAnswer(Answer answer){

    }

    public boolean removeFollowRelationAndUpdate(String usernameFollower, String usernameFollowed){

    }

    public boolean removePost(Post post){

    }

    public boolean removeUser(String username){

    }
}
