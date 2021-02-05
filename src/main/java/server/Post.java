package server;

import java.util.Date;
import java.util.List;

public class Post {

    private String postId;
    private String title;
    private List<Answer> answers;
    private Date creationDate;
    private String body;
    private String ownerUserId;
    private List<String> tags;

    public Post(){

    }

    public Post(String postId, String title, List<Answer> answers, Date creationDate, String body, String ownerUserId, List<String> tags){
        this.postId = postId;
        this.title = title;
        this.answers = answers;
        this.creationDate = creationDate;
        this.body = body;
        this.ownerUserId = ownerUserId;
        this.tags = tags;
    }

    public String getPostId(){
        return postId;
    }

    public String getTitle(){
        return title;
    }

    public List<Answer> getAnswers(){
        return answers;
    }

    public Date getCreationDate(){
        return creationDate;
    }

    public String getBody(){
        return body;
    }

    public String getOwnerUserId(){
        return ownerUserId;
    }

    public List<String> getTags(){
        return tags;
    }

    public void setPostId(String id){
        postId = id;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setAnswers(List<Answer> answers){
        this.answers = answers;
    }

    public void setCreationDate(Date data){
        creationDate = data;
    }

    public void setBody(String body){
        this.body = body;
    }

    public void setOwnerUserId(String userId){
        ownerUserId = userId;
    }

    public void setTags(List<String> tags){
        this.tags = tags;
    }
}
