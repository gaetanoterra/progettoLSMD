package middleware;

import server.Post;

//classe messaggio, utilizzata per richiedere Post in base ad un parametro answer al server
public class MessageGetPostByParameter extends Message{

    private Parameter parameter;
    private String value;
    private Post[] post;

    public MessageGetPostByParameter(Parameter parameter, String value, Post[] post){
        this.opcode = Opcode.Message_Get_Post;
        this.parameter = parameter;
        this.value = value;
        this.post = post;
    }

    public Parameter getParameter() { return parameter; }

    public String getValue() { return value; }

    public Post[] getPost() { return post; }
}
