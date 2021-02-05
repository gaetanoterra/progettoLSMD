package server;

import java.util.Date;

public class User {

    private String userId;
    private String password;
    private String displayName;
    private int followersNumber = 0;
    private int followedNumber = 0;
    private double reputation = 0;
    private Date creationDate;
    private Date lastAccessDate;
    private String type = "generic_user";
    private String location;
    private String aboutMe;
    private String websiteURL;

    public User(){
    }
    public User(String userId, String displayName, String location, String am, String url){
        this.userId = userId;
        this.displayName = displayName;
        this.location = location;
        this.aboutMe = am;
        this.websiteURL = url;
    }

    public String getUserId(){
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public int getFollowersNumber() {
        return followersNumber;
    }

    public int getFollowedNumber() {
        return followedNumber;
    }

    public double getReputation() {
        return reputation;
    }

    public Date getCreationData() {
        return creationDate;
    }

    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public String getType() {
        return type;
    }

    public String getDisplayName(){
        return displayName;
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

    public void setId(String id){
        this.userId = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFollowersNumber(int followersNumber) { this.followersNumber = followersNumber; }

    public void setFollowedNumber(int followedNumber) { this.followedNumber = followedNumber; }

    public void setReputation(double reputation) { this.reputation = reputation; }

    public void setCreationData(Date creationData) { this.creationDate = creationData; }

    public void setLastAccessDate(Date lastAccessDate) { this.lastAccessDate = lastAccessDate; }

    public void setType(String type) { this.type = type; }

    public void setDisplayName(String displayName){
        this.displayName = displayName;
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