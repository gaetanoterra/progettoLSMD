package it.unipi.dii.Libraries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post implements Serializable {

    private String postId;
    private String title;
    private String ownerUserName;
    private List<Answer> answers;
    private Long creationDate;
    private String body;
    private String ownerUserId;
    private List<String> tags;
    private Long views;


    private int answersNumber;

    public Post(){
        this(null, null, new ArrayList<>(), null, null, null, new ArrayList<>());
    }

    public Post(String postId, String title, List<Answer> answers, Long creationDate, String body, String ownerUserId, List<String> tags){
        this.postId = postId;
        this.title = title;
        this.answers = answers;
        this.creationDate = creationDate;
        this.body = body;
        this.ownerUserId = ownerUserId;
        this.tags = tags;
    }

    public Post(String postId, String title, int answersNumber, String ownerUserId, List<String> tags){
        this.postId = postId;
        this.title = title;
        this.answersNumber = answersNumber;
        this.ownerUserId = ownerUserId;
        this.tags = tags;
    }
    public String getPostId(){
        return this.postId;
    }

    public String getTitle(){
        return this.title;
    }

    public List<Answer> getAnswers(){
        return this.answers;
    }

    public Long getCreationDate(){
        return this.creationDate;
    }

    public String getBody(){
        return this.body;
    }

    public String getOwnerUserId(){
        return this.ownerUserId;
    }

    public String getOwnerUserName(){
        return this.ownerUserName;
    }

    public List<String> getTags(){
        return this.tags;
    }

    public Long getViews() { return this.views; }

    public int getAnswersNumber() {
        return answersNumber;
    }

    public Post setPostId(String postId){
        this.postId = postId;
        return this;
    }

    public Post setTitle(String title){
        this.title = title;
        return this;
    }

    public Post setAnswers(List<Answer> answers){
        this.answers.clear();
        if (answers != null) {
            this.answers.addAll(answers);
        }
        return this;
    }

    public Post setCreationDate(Long creationDate){
        this.creationDate = creationDate;
        return this;
    }

    public Post setBody(String body){
        this.body = body;
        return this;
    }

    public Post setOwnerUserId(String ownerUserId){
        this.ownerUserId = ownerUserId;
        return this;
    }

    public Post setTags(List<String> tags){
        this.tags.clear();
        if (tags != null) {
            this.tags.addAll(tags);
        }
        return this;
    }

    public Post setViews(Long views) {
        this.views = views;
        return this;
    }

    public Post setOwnerUserName(String ownerUserName) {
        this.ownerUserName = ownerUserName;
        return this;
    }

    public Post setAnswersNumber(int answersNumber) {
        this.answersNumber = answersNumber;
        return this;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId='" + postId + '\'' +
                ", title='" + title + '\'' +
                ", answers=" + answers +
                ", creationDate=" + creationDate +
                ", body='" + body + '\'' +
                ", ownerUserId='" + ownerUserId + '\'' +
                ", tags=" + tags +
                ", views=" + views +
                ", answersNumber=" + answersNumber +
                ", answers=" + answers +
                '}';
    }
}
