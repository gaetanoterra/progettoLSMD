package it.unipi.dii.Libraries;

import java.io.Serializable;
import java.util.Date;

public class Answer implements Serializable {

    private Integer answerId;
    private String creationDate;
    private Integer score;
    private String ownerUserName;
    private String body;

    public Answer(Integer answerId, String creationDate, Integer score, String ownerUserName, String body) {
        this.answerId = answerId;
        this.creationDate = creationDate;
        this.score = score;
        this.ownerUserName = ownerUserName;
        this.body = body;
    }

    public Answer(Integer id, Date creationDate, Integer score, String ownerDisplayName, String body) {
        this(id,creationDate.toString(), score, ownerDisplayName,body);
    }

    public Integer getAnswerId(){
        return this.answerId;
    }

    public String getCreationDate(){
        return this.creationDate;
    }

    public Integer getScore(){
        return this.score;
    }

    public String getOwnerUserName(){
        return this.ownerUserName;
    }

    public String getBody() { return this.body; }

    public Answer setAnswerId(Integer answerId){
        this.answerId = answerId;
        return this;
    }

    public Answer setCreationDate(String creationDate){
        this.creationDate = creationDate;
        return this;
    }

    public Answer setScore(Integer score){
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

    @Override
    public String toString() {
        return "{" +
                "answerId='" + answerId + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", score=" + score +
                ", ownerUserName='" + ownerUserName + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
