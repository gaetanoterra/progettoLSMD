package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;

import java.util.ArrayList;

//classe inutilizzata?
public class MessageGetPostData extends MessageReadObjectQuery {

    private Post post;

    //usato per la richiesta del client
    public MessageGetPostData(){
        this(null);
    }

    //usato per la risposta dal server
    public MessageGetPostData(Post p){
        this.opcode = Opcode.Message_Get_Post_Data;
        this.post = p;
    }

    @Override
    public Post getObject() {
        return this.post;
    }

    @Override
    public String toString() {
        return "MessageGetPostData{\n" +
               "opcode=" + opcode + "\n" +
               "object=" + this.post +
               "\n}";
    }
}
