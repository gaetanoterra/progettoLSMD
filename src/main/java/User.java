public class User {

    private int userId;
    private String username;
    private String location;
    private String aboutMe;
    private String websiteURL;

    public User(int userId, String username, String location, String am, String url){

    }

    public int getId(){
        return userId;
    }

    public String getUsername(){
        return username;
    }

    public String getLocation(){
        return location;
    }

    public String getAboutMe(){
        return aboutMe;
    }

    public String getWebsiteURL(){
        return websiteURL;
    }

    public void setId(int id){
        this.userId = id;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public void setAboutMe(String aboutMe){
        this.aboutMe = aboutMe;
    }

    public void setWebsiteURL(String websiteURL){
        this.websiteURL = websiteURL;
    }
}
