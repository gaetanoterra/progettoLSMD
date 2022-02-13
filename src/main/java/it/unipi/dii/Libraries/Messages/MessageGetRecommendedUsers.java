package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.User;

import java.util.ArrayList;

public class MessageGetRecommendedUsers extends Message{

    private ArrayList<User> users;
    private String displayName;
    private String tag;

    public MessageGetRecommendedUsers(String displayName, String tag, ArrayList<User> users){
        this.opcode = Opcode.Message_Get_Recommended_Users;
        this.users = users;
        this.tag = tag;
        this.displayName = displayName;
    }

    @Override
    public Opcode getOpcode() { return super.getOpcode(); }

    public String getDisplayName() { return displayName; }

    public ArrayList<User> getUsers() { return users; }

    public String getTag() { return tag; }

    public void setUsers(ArrayList<User> users) { this.users = users; }
}
