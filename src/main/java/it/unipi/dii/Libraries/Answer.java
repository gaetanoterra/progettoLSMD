package it.unipi.dii.Libraries;

import java.io.Serializable;

public class Answer implements Serializable {

    //TODO: Considerare possibile cambio in postId (riutilizzare Id come postId di cui è risposta) oppure indice nell'array delle risposte
    private String answerId;
    private Long creationDate;
    private Integer score;
    private String ownerUserId;
    private String ownerUserName;
    private String body;
    // Attualmente è utilizzato per rendere possibile l'upvote/downvote, non è inserito nel db
    private String postId;

    public Answer(String answerId, Long creationDate, Integer score, String ownerUserId, String ownerUserName, String body, String postId) {
        this.answerId = answerId;
        this.creationDate = creationDate;
        this.score = score;
        this.ownerUserId = ownerUserId;
        this.ownerUserName = ownerUserName;
        this.body = body;
        this.postId = postId;
    }

    public Answer(String answerId, Long creationDate, Integer score, String ownerUserId, String ownerUserName, String body) {
        this(answerId, creationDate, score, ownerUserId, ownerUserName, body, null);
    }

    public Answer(String body) {
        this(null, null, null, null, null, body, null);
    }

    public String getAnswerId(){
        return this.answerId;
    }

    public Long getCreationDate(){
        return this.creationDate;
    }

    public Integer getScore(){
        return this.score;
    }

    public String getOwnerUserName(){
        return this.ownerUserName;
    }

    public String getBody() { return this.body; }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getPostId() {
        return postId;
    }

    public Answer setAnswerId(String answerId){
        this.answerId = answerId;
        return this;
    }

    public Answer setCreationDate(Long creationDate){
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

    public Answer setBody(String body) {
        this.body = body;
        return this;
    }

    public Answer setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
        return this;
    }

    public Answer setPostId(String postId) {
        this.postId = postId;
        return this;
    }


    @Override
    public String toString() {
        return "{" +
                "answerId='" + answerId + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", score=" + score +
                ", ownerUserName='" + ownerUserName + '\'' +
                ", body='" + body + '\'' +
                ", postId='" + postId + '\'' +
                '}';
    }
}
