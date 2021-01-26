import java.io.Serializable;
import java.util.HashMap;

public class MessageGetTopUsersPosts extends Message implements Serializable {

    private HashMap<User, Post[]> mostAnsweredTopUserPostsMap;
    private String username;

    public HashMap<User, Post[]> getMostAnsweredTopUserPostsMap() {
        return mostAnsweredTopUserPostsMap;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public byte getOpcode() {
        return super.getOpcode();
    }
}
