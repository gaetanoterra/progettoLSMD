package middleware;

import client.*;
import server.*;

public class MessageCreatePost extends MessageCreateDelete{

    private Opcode opcode;
    private Post post;

    public MessageCreatePost(Opcode opcode, Post post){
        this.opcode = opcode;
        this.post = post;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
