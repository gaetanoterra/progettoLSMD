package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;

import java.util.ArrayList;

public class MessageGetRecommendedUsers extends Message{

    private ArrayList<String> users;
    private String displayName;
    private String tag;

    public MessageGetRecommendedUsers(String displayName, String tag, ArrayList<String> users){
        this.opcode = Opcode.Message_Get_Recommended_Users;
        this.users = users;
        this.tag = tag;
        this.displayName = displayName;
    }

    @Override
    public Opcode getOpcode() { return super.getOpcode(); }

    public String getDisplayName() { return displayName; }

    public ArrayList<String> getUsers() { return users; }

    public String getTag() { return tag; }

    public void setUsers(ArrayList<String> users) { this.users = users; }
}
