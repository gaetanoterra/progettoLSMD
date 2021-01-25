public class DBManager {

    private DocumentDBManager documentDBMan;
    private GraphDBManager graphDBMan;

    public DBManager(){

    }

    public User[] findMostPopularTagsByLocation(String tag, int idUser){

        return user;
    }

    public Map<User, Post> findMostAnsweredTopUserPosts(){
        return user;
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

    public User GetUserData(String username){

    }

    public User[] getUsersRank(){

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

    public boolean updateUserData(User user){

    }
}
