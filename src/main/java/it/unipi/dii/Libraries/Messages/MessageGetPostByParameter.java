package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;

import java.util.ArrayList;
import java.util.Arrays;

//classe messaggio, utilizzata per richiedere Post in base ad un parametro answer al server
public class MessageGetPostByParameter extends Message{

    private Parameter parameter;
    private String value;
    private ArrayList<Post> postArrayList;

    public MessageGetPostByParameter(Parameter parameter, String value){
        this.opcode = Opcode.Message_Get_Posts_By_Parameter;
        this.parameter = parameter;
        this.value = value;
        this.postArrayList = null;
    }

    public MessageGetPostByParameter(Parameter parameter, String value, Post[] post){
        this.opcode = Opcode.Message_Get_Posts_By_Parameter;
        this.parameter = parameter;
        this.value = value;
        this.postArrayList = new ArrayList<>(Arrays.asList(post));
    }

    public MessageGetPostByParameter(Parameter parameter, String value, ArrayList<Post> post){
        this.opcode = Opcode.Message_Get_Posts_By_Parameter;
        this.parameter = parameter;
        this.value = value;
        this.postArrayList = post;
    }

    public Parameter getParameter() { return parameter; }

    public String getValue() { return value; }

    public ArrayList<Post> getPostArrayList(){ return this.postArrayList;}

    @Override
    public String toString() {
        return "MessageGetPostByParameter{" +
                "parameter=" + parameter +
                '}';
    }
}
