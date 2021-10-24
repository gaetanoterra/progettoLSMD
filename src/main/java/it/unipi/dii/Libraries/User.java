package it.unipi.dii.Libraries;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

    //TODO nel database ci sono 3 identificativi (_id, Id, AccountId) e manca lastAccessDate e type
    private Integer userId;
    private String password;
    private String displayName;
    private Integer followersNumber;
    private Integer followedNumber;
    private Integer reputation;
    private Long creationDate;
    private Long lastAccessDate;
    private String type = "generic_user";
    private String location;
    private String aboutMe;
    private String websiteURL;
    private String profileImage;

    public User(Integer userId, String displayName, String location, String aboutMe, String websiteURL, String pwd){
        this.userId = userId;
        this.displayName = displayName;
        this.location = location;
        this.aboutMe = aboutMe;
        this.websiteURL = websiteURL;
        this.password = pwd;
    }

    public User(Integer userId, String displayName, String location, String aboutMe, String websiteURL){
        this(userId, displayName, location, aboutMe,  websiteURL, null);
    }

    public User(){
        this(null,null,null,null,null);
    }
    public Integer getUserId(){
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

    public int getReputation() {
        return this.reputation;
    }

    public Long getCreationDate() {
        return this.creationDate;
    }

    public Long getLastAccessDate() {
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public User setUserId(Integer userId){
        this.userId = userId;
        return this;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public User setFollowersNumber(Integer followersNumber) {
        this.followersNumber = followersNumber;
        return this;
    }

    public User setFollowedNumber(Integer followedNumber) {
        this.followedNumber = followedNumber;
        return this;
    }

    public User setReputation(Integer reputation) {
        this.reputation = reputation;
        return this;
    }

    public User setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public User setLastAccessDate(Long lastAccessDate) {
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

    public static Date convertMillisToDate (long millis){
        Date data = new Date(millis);

        System.out.println(data);

        return data;
    }
}