package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.User;

import java.util.ArrayList;

public class MessageGetCorrelatedUsers extends MessageReadObjectQuery {
    private ArrayList<User> userList;
    private String user;

    public MessageGetCorrelatedUsers(ArrayList<User> userList, String user){
        this.opcode = Opcode.Message_Get_Correlated_Users;
        this.userList = userList;
        this.user = user;
    }

    public ArrayList<User> getObject() {
        return this.userList;
    }

    @Override
    public String toString() {
        return "MessageGetUserData{" +
                "opcode=" + opcode.name() +
                '}';
    }

    public void setUserList(ArrayList<User> userList) { this.userList = userList; }

    public String getUser() { return user; }
}
