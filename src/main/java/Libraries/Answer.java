package Libraries;

public class Answer {

    private String answerId;
    private String creationDate;
    private double score;
    private String ownerUserId;
    private String body;

    public Answer(){

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

    public String getOwnerUserId(){
        return this.ownerUserId;
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

    public Answer setOwnerUserId(String ownerUserId){
        this.ownerUserId = ownerUserId;
        return this;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
