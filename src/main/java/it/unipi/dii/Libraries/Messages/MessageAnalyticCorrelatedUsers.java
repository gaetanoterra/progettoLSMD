package it.unipi.dii.Libraries.Messages;

public class MessageAnalyticCorrelatedUsers extends Message {
    private String username;
    private String[] users;
    private int numUsers = 10;

    public MessageAnalyticCorrelatedUsers (String username, String[] users){
        this.opcode = Opcode.Message_Analytics_Correlated_Users;
        this.username = username;
        this.users = users;
    }

    @Override
    public Opcode getOpcode() { return super.getOpcode(); }

    public String getUsername() { return username; }

    public String[] getUsers() { return users; }

    public void setNumUsers(int numUsers) { this.numUsers = numUsers; }

    public void setUsername(String username) { this.username = username; }

    public void setUsers(String[] users) { this.users = users; }
}
