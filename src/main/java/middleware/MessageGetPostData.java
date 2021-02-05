package middleware;

import client.*;
import server.*;

public class MessageGetPostData extends MessageReadObjectQuery {

    Opcode opcode;
    private Post[] post;

    //usato per la richiesta del client
    public MessageGetPostData(Opcode opcode){
        this.opcode = opcode;
    }

    //usato per la risposta dal server
    public MessageGetPostData(Opcode opcode, Post[] post){
        this.opcode = opcode;
        this.post = post;
    }
    @Override
    public Opcode getOpcode() {
        return super.getOpcode();
    }

    @Override
    public Object getObject() {
        return super.getObject();
    }
}
