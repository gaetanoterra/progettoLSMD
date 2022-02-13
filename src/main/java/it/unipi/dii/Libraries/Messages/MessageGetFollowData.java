package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.User;

import java.util.ArrayList;

public class MessageGetFollowData extends Message{
    private ArrayList<User> followers;
    private String userDisplayName;
    private Boolean type; //true se voglio i miei followers, false se voglio chi mi followa

    //usato per la risposta dal server
    public MessageGetFollowData(ArrayList<User> followers, String displayName, Boolean type){
        this.opcode = Opcode.Message_Get_Follow_Data;
        this.followers = followers;
        this.userDisplayName = displayName;
        this.type = type;
    }

    public void setFollowers(ArrayList<User> followers) {
        this.followers = followers;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public ArrayList<User> getFollowers() {
        return followers;
    }

    public Boolean getType() {
        return type;
    }

    @Override
    public String toString() {
        return "MessageGetPostData{\n" +
                "opcode=" + opcode.name() + "\n" +
                "}";
    }
}
