package it.unipi.dii.Libraries.Messages;

public class MessageAnalyticRecommendedUsers extends Message {
    private String username;
    private String tag;
    private String[] users;
    private int numUSers = 10;

    public MessageAnalyticRecommendedUsers (String username,String tag, String[] users){
        this.opcode = Opcode.Message_Analytics_Recommended_Users;
        this.username = username;
        this.tag = tag;
        this.users = users;
    }

    public String getUsername() { return username; }

    public String getTag() { return tag; }

    public String[] getUsers() { return users; }

    public void setUsers(String[] users) { this.users = users; }

    public void setNumUSers(int numUSers) { this.numUSers = numUSers; }

    public void setTag(String tag) { this.tag = tag; }

    public void setUsername(String username) { this.username = username; }
}
