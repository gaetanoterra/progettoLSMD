package middleware;

import server.*;

public class MesssageGetUserData extends MessageReadObjectQuery {
    private User[] userList;

    public MesssageGetUserData(){
        this(null);
    }

    //usato per la risposta dal server
    public MesssageGetUserData(User[] userList){
        this.opcode = Opcode.Message_Get_User_Data;
        this.userList = userList;
    }

    @Override
    public User[] getObject() {
        return this.userList;
    }
}
