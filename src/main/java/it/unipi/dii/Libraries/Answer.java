package it.unipi.dii.Libraries;

import java.io.Serializable;
import java.util.Date;

public class Answer implements Serializable {

    private String answerId;
    private String creationDate;
    private double score;
    private String ownerUserName;
    private String body;

    public Answer(String answerId, String creationDate, double score, String ownerUserName, String body) {
        this.answerId = answerId;
        this.creationDate = creationDate;
        this.score = score;
        this.ownerUserName = ownerUserName;
        this.body = body;
    }

    public Answer(String id, Date creationDate, Double viewCount, String ownerDisplayName, String body) {
        this(null,null, 0, null,null);
    }

    public String getAnswerId(){
        return this.answerId;
    }

    public String getCreationDate(){
        return this.creationDate;
    }

    public double getScore(){
        return this.score;
    }

    public String getOwnerUserName(){
        return this.ownerUserName;
    }

    public String getBody() { return this.body; }

    public Answer setAnswerId(String answerId){
        this.answerId = answerId;
        return this;
    }

    public Answer setCreationDate(String creationDate){
        this.creationDate = creationDate;
        return this;
    }

    public Answer setScore(double score){
        this.score = score;
        return this;
    }

    public Answer setOwnerUserName(String ownerUserName){
        this.ownerUserName = ownerUserName;
        return this;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
