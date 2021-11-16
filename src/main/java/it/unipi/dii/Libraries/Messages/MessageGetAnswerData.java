package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;

import java.util.ArrayList;

public class MessageGetAnswerData extends Message{

    private ArrayList<Post> answers;
    private String displayName;

    public MessageGetAnswerData(ArrayList<Post> answers, String userDisplayName){
        this.opcode = Opcode.Message_Get_Answer_Data;
        this.answers = answers;
        this.displayName = userDisplayName;
    }

    @Override
    public Opcode getOpcode() { return super.getOpcode(); }

    public String getDisplayName() { return displayName; }

    public ArrayList<Post> getAnswers() { return answers; }

    public void setAnswers(ArrayList<Post> answers) { this.answers = answers; }
}
