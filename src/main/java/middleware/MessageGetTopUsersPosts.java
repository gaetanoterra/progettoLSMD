package middleware;

import server.*;

import java.util.HashMap;

//classe messaggio, utilizzata per richiedere utenti e post più popolari al server
public class MessageGetTopUsersPosts extends Message {

    private HashMap<User, Post[]> mostAnsweredTopUsersPostsMap;

    public MessageGetTopUsersPosts() {
        this(null);
    }
    public MessageGetTopUsersPosts(HashMap<User, Post[]> map) {
        this.opcode = Opcode.Message_Get_Top_Users_Posts;
        this.mostAnsweredTopUsersPostsMap = map;
    }
    public HashMap<User, Post[]> getMostAnsweredTopUsersPostsMap() {
        return mostAnsweredTopUsersPostsMap;
    }

}
