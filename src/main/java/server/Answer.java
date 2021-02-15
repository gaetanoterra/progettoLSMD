package server;

public class Answer {

    private String answerId;
    private String creationDate;
    private double score;
    private String ownerUserId;
    private String body;

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

    public String getOwnerUserId(){
        return ownerUserId;
    }

    public String getBody() { return body; }

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

    public Answer setOwnerUserId(String userId){
        ownerUserId = userId;
        return this;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
