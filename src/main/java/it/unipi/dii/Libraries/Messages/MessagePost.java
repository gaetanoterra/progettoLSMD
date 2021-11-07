package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;

//classe messaggio, utilizzata per inviare una richiesta di creazione/eliminazione Post al server
public class MessagePost extends MessageCreateDelete{

    private Post post;

    public MessagePost(OperationCD operation, Post post){
        this.opcode = Opcode.Message_Post;
        this.operation = operation;
        this.post = post;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @Override
    public Object getObject() {
        return getPost();
    }

    @Override
    public String toString() {
        return "MessagePost{" +
                "opcode=" + opcode.name() +
                '}';
    }
}
