package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//classe messaggio, utilizzata per richiedere Post in base ad un parametro answer al server
public class MessageGetPostByParameter extends Message{

    private Parameter parameter;
    private String value;
    private List<Post> postList;

    public MessageGetPostByParameter(Parameter parameter, String value){
        this.opcode = Opcode.Message_Get_Posts_By_Parameter;
        this.parameter = parameter;
        this.value = value;
        this.postList = null;
    }

    public MessageGetPostByParameter(Parameter parameter, String value, Post[] post){
        this.opcode = Opcode.Message_Get_Posts_By_Parameter;
        this.parameter = parameter;
        this.value = value;
        this.postList = new ArrayList<>(Arrays.asList(post));
    }

    public MessageGetPostByParameter(Parameter parameter, String value, List<Post> post){
        this.opcode = Opcode.Message_Get_Posts_By_Parameter;
        this.parameter = parameter;
        this.value = value;
        this.postList = post;
    }

    public Parameter getParameter() { return parameter; }

    public String getValue() { return value; }

    public List<Post> getPostList(){ return this.postList;}

    @Override
    public String toString() {
        return "MessageGetPostByParameter{" +
                "parameter=" + parameter.name() +
                '}';
    }
}
