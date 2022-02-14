package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;

import java.util.ArrayList;
import java.util.Arrays;

//classe messaggio, utilizzata per richiedere Post in base a un parametro answer al server
public class MessageGetPostsByParameter extends MessageReadObjectQuery{

    private Parameter parameter;
    private String value;
    private ArrayList<Post> postList;

    public MessageGetPostsByParameter(Parameter parameter, String value){
        this(parameter, value, new ArrayList<>());
    }

    public MessageGetPostsByParameter(Parameter parameter, String value, Post[] post){
        this(parameter, value, new ArrayList<>(Arrays.asList(post)));
    }

    public MessageGetPostsByParameter(Parameter parameter, String value, ArrayList<Post> post){
        this.opcode = Opcode.Message_Get_Posts_By_Parameter;
        this.parameter = parameter;
        this.value = value;
        this.postList = post;
    }

    @Override
    public ArrayList<Post> getObject(){ return this.postList;}

    public Parameter getParameter() { return parameter; }

    public String getValue() { return value; }

    @Override
    public String toString() {
        return "MessageGetPostByParameter{" +
                "parameter=" + parameter.name() +
                '}';
    }
}
