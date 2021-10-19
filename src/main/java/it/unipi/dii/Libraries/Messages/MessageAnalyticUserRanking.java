package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.User;

public class MessageAnalyticUserRanking extends Message {
    private int numUsers = 10;
    private User[] users;

    public MessageAnalyticUserRanking (User[] users){
        this.opcode = Opcode.Message_Analytics_User_Rank;
        this.users = users;
    }

    @Override
    public Opcode getOpcode() { return super.getOpcode(); }

    public User[] getUsers() { return users; }

    public void setUsers(User[] users) { this.users = users; }
}
