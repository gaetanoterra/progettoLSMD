package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Answer;

import java.util.ArrayList;

public class MessageGetAnswers extends MessageReadObjectQuery{
    private String displayName;
    private ArrayList<Answer> answerArrayList;

    public MessageGetAnswers(String displayName){
        this(displayName, new ArrayList<>());
    }

    public MessageGetAnswers(String displayName, ArrayList<Answer> answers){
        this.opcode = Opcode.Message_Get_User_Answers;
        this.displayName = displayName;
        this.answerArrayList = answers;
    }

    @Override
    public ArrayList<Answer> getObject(){ return this.answerArrayList;}

    public String getDisplayName() { return displayName; }

    public void setAnswerArrayList(ArrayList<Answer> answerArrayList) { this.answerArrayList = answerArrayList; }
}
