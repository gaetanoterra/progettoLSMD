package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;

import java.util.ArrayList;
import java.util.Arrays;

public class MessageGetAnswers extends Message{
    private String displayName;
    private ArrayList<Post> answerArrayList;

    public MessageGetAnswers(String displayName){
        this.opcode = Opcode.Message_Get_User_Answers;
        this.displayName = displayName;
        this.answerArrayList = null;
    }

    /*public MessageGetAnswers(String displayName, Post[] post){
        this.opcode = Opcode.Message_Get_Posts_By_Parameter;
        this.displayName = displayName;
        this.postArrayList = new ArrayList<>(Arrays.asList(post));
    }*/

    public MessageGetAnswers(String displayName, ArrayList<Post> post){
        this.opcode = Opcode.Message_Get_User_Answers;
        this.displayName = displayName;
        this.answerArrayList = post;
    }

    public String getDisplayName() { return displayName; }

    public ArrayList<Post> getAnswerArrayList(){ return this.answerArrayList;}

    public void setAnswerArrayList(ArrayList<Post> answerArrayList) { this.answerArrayList = answerArrayList; }
}
