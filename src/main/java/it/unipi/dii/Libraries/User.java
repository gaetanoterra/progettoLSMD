package it.unipi.dii.Libraries;

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
    public User(String userId, String displayName, String location, String aboutMe, String websiteURL){
        this.userId = userId;
        this.displayName = displayName;
        this.location = location;
        this.aboutMe = aboutMe;
        this.websiteURL = websiteURL;
    }

    public String getUserId(){
        return this.userId;
    }

    public String getPassword() {
        return this.password;
    }

    public int getFollowersNumber() {
        return this.followersNumber;
    }

    public int getFollowedNumber() {
        return this.followedNumber;
    }

    public double getReputation() {
        return this.reputation;
    }

    public Date getCreationData() {
        return this.creationDate;
    }

    public Date getLastAccessDate() {
        return this.lastAccessDate;
    }

    public String getType() {
        return this.type;
    }

    public String getDisplayName(){
        return this.displayName;
    }

    public String getLocation(){
        return this.location;
    }

    public String getAboutMe(){
        return this.aboutMe;
    }

    public String getWebsiteURL(){
        return this.websiteURL;
    }

    public User setUserId(String userId){
        this.userId = userId;
        return this;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public User setFollowersNumber(int followersNumber) {
        this.followersNumber = followersNumber;
        return this;
    }

    public User setFollowedNumber(int followedNumber) {
        this.followedNumber = followedNumber;
        return this;
    }

    public User setReputation(double reputation) {
        this.reputation = reputation;
        return this;
    }

    public User setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public User setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
        return this;
    }

    public User setType(String type) {
        this.type = type;
        return this;
    }

    public User setDisplayName(String displayName){
        this.displayName = displayName;
        return this;
    }

    public User setLocation(String location){
        this.location = location;
        return this;
    }

    public User setAboutMe(String aboutMe){
        this.aboutMe = aboutMe;
        return this;
    }

    public User setWebsiteURL(String websiteURL){
        this.websiteURL = websiteURL;
        return this;
    }
}