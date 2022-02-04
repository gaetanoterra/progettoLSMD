package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Answer;
import it.unipi.dii.Libraries.Post;

import java.util.ArrayList;
import java.util.Arrays;

public class MessageGetAnswers extends Message{
    private String displayName;
    private ArrayList<Answer> answerArrayList;

    public MessageGetAnswers(String displayName){
        this.opcode = Opcode.Message_Get_User_Answers;
        this.displayName = displayName;
        this.answerArrayList = null;
    }

    public MessageGetAnswers(String displayName, ArrayList<Answer> answers){
        this.opcode = Opcode.Message_Get_User_Answers;
        this.displayName = displayName;
        this.answerArrayList = answers;
    }

    public String getDisplayName() { return displayName; }

    public ArrayList<Answer> getAnswerArrayList(){ return this.answerArrayList;}

    public void setAnswerArrayList(ArrayList<Answer> answerArrayList) { this.answerArrayList = answerArrayList; }
}
