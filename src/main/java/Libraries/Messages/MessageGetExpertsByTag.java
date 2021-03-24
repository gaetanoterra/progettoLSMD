package Libraries.Messages;

import Libraries.User;

import java.util.ArrayList;

//classe messaggio, utilizzata per richiedere utenti per tag al server
public class MessageGetExpertsByTag extends Message {

    private String tag;
    private ArrayList<User> usersList;

    //usato per la richiesta del client
    public MessageGetExpertsByTag(String tag){
        this(tag, null);
    }

    //usato per la risposta del server
    public MessageGetExpertsByTag(String tag, ArrayList<User> usersList){
        this.opcode = Opcode.Message_Get_Experts;
        this.tag = tag;
        this.usersList = usersList;
    }

    public ArrayList<User> getUsersList() {
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
