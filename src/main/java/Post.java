import java.util.ArrayList;

public class Post {

    private int postId;
    private String title;
    private ArrayList<Answer> answers;
    private String creationDate;
    private String body;
    private int ownerUserId;
    private ArrayList<String> tags;

    public Post(){

    }

    public int getPostId(){
        return postId;
    }

    public String getTitle(){
        return title;
    }

    public ArrayList<Answer> getAnswers(){
        return answers;
    }

    public String getCreationDate(){
        return creationDate;
    }

    public String getBody(){
        return body;
    }

    public int getOwnerUserId(){
        return ownerUserId;
    }

    public ArrayList<String> getTags(){
        return tags;
    }

    public void setPostId(int id){
        postId = id;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setAnswers(ArrayList<Answer> answers){
        this.answers = answers;
    }

    public void setCreationDate(String data){
        creationDate = data;
    }

    public void setBody(String body){
        this.body = body;
    }

    public void setOwnerUserId(int userId){
        ownerUserId = userId;
    }

    public void setTags(ArrayList<String> tags){
        this.tags = tags;
    }
}
