public class DocumentDBManager {

    private MongoClient dbConnection;
    private MongoDatabase database;

    public DocumentDBManager(){

    }

    public User[] findMostPopularTagsByLocation(String tag, int idUser){

    }

    public User[] findTopExpertsByTag(String tag, int idUser){

    }

    public Post getPostById(int postId){

    }

    public Post[] getPostsByTag(String[] tags){

    }

    public Post getPostByText(String text){

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
