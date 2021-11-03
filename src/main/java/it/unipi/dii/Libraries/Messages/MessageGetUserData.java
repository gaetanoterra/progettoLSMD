package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.User;

import java.util.ArrayList;

//classe messaggio, utilizzata per inviare una richiesta dati utente al server
public class MessageGetUserData extends MessageReadObjectQuery {
    private ArrayList<User> userList;
    private boolean profileType;  //il profile type indica se i dati che vogliamo sono i nostri o di un utente di cui vogliamo vedere il profilo

    //usato per la risposta dal server
    public MessageGetUserData(ArrayList<User> userList,boolean profileType){
        this.opcode = Opcode.Message_Get_User_Data;
        this.userList = userList;
        this.profileType = profileType;
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

    public boolean getProfileType() {
        return profileType;
    }
}
