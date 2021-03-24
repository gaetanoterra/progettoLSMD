package Libraries;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Post  implements Serializable {

    private String postId;
    private String title;
    private List<Answer> answers;
    private Date creationDate;
    private String body;
    private String ownerUserId;
    private List<String> tags;
    private int views;

    public Post(){
        this(null, null, null, null, null, null, null);
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
        return this.postId;
    }

    public String getTitle(){
        return this.title;
    }

    public List<Answer> getAnswers(){
        return this.answers;
    }

    public Date getCreationDate(){
        return this.creationDate;
    }

    public String getBody(){
        return this.body;
    }

    public String getOwnerUserId(){
        return this.ownerUserId;
    }

    public List<String> getTags(){
        return this.tags;
    }

    public int getViews() { return this.views; }

    public Post setPostId(String postId){
        this.postId = postId;
        return this;
    }

    public Post setTitle(String title){
        this.title = title;
        return this;
    }

    public Post setAnswers(List<Answer> answers){
        this.answers = answers;
        return this;
    }

    public Post setCreationDate(Date creationDate){
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
        this.tags = tags;
        return this;
    }

    public Post setViews(int views) {
        this.views = views;
        return this;
    }
}
