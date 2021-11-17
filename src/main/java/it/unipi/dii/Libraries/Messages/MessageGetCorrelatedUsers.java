package it.unipi.dii.Libraries.Messages;

import java.util.ArrayList;

public class MessageGetCorrelatedUsers extends Message {
    private ArrayList<String> userList;
    private String user;
    public MessageGetCorrelatedUsers(ArrayList<String> userList, String user){
        this.opcode = Opcode.Message_Get_Correlated_Users;
        this.userList = userList;
        this.user = user;
    }

    public ArrayList<String> getObject() {
        return this.userList;
    }

    @Override
    public String toString() {
        return "MessageGetUserData{" +
                "opcode=" + opcode.name() +
                '}';
    }

    public void setUserList(ArrayList<String> userList) { this.userList = userList; }

    public String getUser() { return user; }
}
