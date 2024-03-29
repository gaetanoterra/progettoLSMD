package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//classe messaggio, utilizzata per richiedere utenti e post più popolari al server
public class MessageGetTopUsersPosts extends MessageReadObjectQuery {

    private HashMap<User, ArrayList<Post>>  mostAnsweredTopUsersPostsMap;

    public MessageGetTopUsersPosts() {
        this(null);
    }

    public MessageGetTopUsersPosts(HashMap<User, ArrayList<Post>> map) {
        this.opcode = Opcode.Message_Get_Top_Users_Posts;
        this.mostAnsweredTopUsersPostsMap = map;
    }

    @Override
    public HashMap<User, ArrayList<Post>>  getObject() {
        return mostAnsweredTopUsersPostsMap;
    }

    @Override
    public String toString() {
        return "MessageGetTopUsersPosts{" +
                "opcode=" + opcode.name() +
                '}';
    }
}
