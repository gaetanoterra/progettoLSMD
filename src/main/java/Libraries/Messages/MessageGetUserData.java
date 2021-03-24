package Libraries.Messages;

import Libraries.User;

import java.util.ArrayList;

//classe messaggio, utilizzata per inviare una richiesta dati utente al server
public class MessageGetUserData extends MessageReadObjectQuery {
    private ArrayList<User> userList;

    public MessageGetUserData(){
        this(null);
    }

    //usato per la risposta dal server
    public MessageGetUserData(ArrayList<User> userList){
        this.opcode = Opcode.Message_Get_User_Data;
        this.userList = userList;
    }

    @Override
    public ArrayList<User> getObject() {
        return this.userList;
    }

    @Override
    public String toString() {
        return "MessageGetUserData{" +
                "opcode=" + opcode +
                '}';
    }
}
