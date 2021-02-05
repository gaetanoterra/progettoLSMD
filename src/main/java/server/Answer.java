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

    public void setAnswerId(String answerId){
        this.answerId = answerId;
    }

    public void setCreationDate(String data){
        creationDate = data;
    }

    public void setScore(double score){
        this.score = score;
    }

    public void setOwnerUserId(int userId){
        ownerUserId = userId;
    }
}
