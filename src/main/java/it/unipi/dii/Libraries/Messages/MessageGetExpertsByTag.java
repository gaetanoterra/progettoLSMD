package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.User;

import java.util.ArrayList;

//classe messaggio, utilizzata per richiedere utenti per tag al server
public class MessageGetExpertsByTag extends Message {

    private String tag;
    private String[] usersList;

    //usato per la richiesta del client
    public MessageGetExpertsByTag(String tag){
        this(tag, null);
    }

    //usato per la risposta del server
    public MessageGetExpertsByTag(String tag, String[] usersList){
        this.opcode = Opcode.Message_Get_Experts;
        this.tag = tag;
        this.usersList = usersList;
    }

    public String[] getUsersList() {
        return usersList;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "MessageGetExpertsByTag{" +
                "opcode=" + opcode +
                '}';
    }
}
