package middleware;

import server.*;

import java.util.ArrayList;

public class MesssageGetUserData extends MessageReadObjectQuery {
    private ArrayList<User> userList;

    public MesssageGetUserData(){
        this(null);
    }

    //usato per la risposta dal server
    public MesssageGetUserData(ArrayList<User> userList){
        this.opcode = Opcode.Message_Get_User_Data;
        this.userList = userList;
    }

    @Override
    public ArrayList<User> getObject() {
        return this.userList;
    }
}
