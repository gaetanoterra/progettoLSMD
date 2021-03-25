package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;

import java.util.ArrayList;

//classe inutilizzata?
public class MessageGetPostData extends MessageReadObjectQuery {

    private ArrayList<Post> postList;

    //usato per la richiesta del client
    public MessageGetPostData(){
        this(null);
    }

    //usato per la risposta dal server
    public MessageGetPostData(ArrayList<Post> postList){
        this.opcode = Opcode.Message_Get_Post_Data;
        this.postList = postList;
    }

    @Override
    public ArrayList<Post> getObject() {
        return this.postList;
    }

    @Override
    public String toString() {
        return "MessageGetPostData{" +
                "opcode=" + opcode +
                '}';
    }
}
