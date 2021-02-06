package server;

import javafx.event.ActionEvent;

public class Answer {

    private String answerId;
    private String creationDate;
    private double score;
    private int ownerUserId;

    public Answer(){

    }

    public String getAnswerId(){
        return answerId;
    }

    public String getCreationDate(){
        return creationDate;
    }

    public double getScore(){
        return score;
    }

    public int getOwnerUserId(){
        return ownerUserId;
    }

    public Answer setAnswerId(String answerId){
        this.answerId = answerId;
        return this;
    }

    public Answer setCreationDate(String data){
        creationDate = data;
        return this;
    }

    public Answer setScore(double score){
        this.score = score;
        return this;
    }

    public Answer setOwnerUserId(int userId){
        ownerUserId = userId;
        return this;
    }
}
