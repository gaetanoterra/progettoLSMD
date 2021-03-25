package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.User;

//classe messaggio, utilizzata per inviare una richiesta di creazione/eliminazione User al server e dalla funzione di modifica utente
public class MessageUser extends MessageCreateDelete{

    private User user;

    public MessageUser(OperationCD operation, User user){
        this.opcode = Opcode.Message_Post;
        this.operation = operation;
        this.user = user;
    }

    public MessageUser(Opcode opcode, OperationCD operation, User user){
        this.opcode = opcode;
        this.operation = operation;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Object getObject() {
        return getUser();
    }

    @Override
    public String toString() {
        return "MessageUser{" +
                "opcode=" + opcode +
                '}';
    }
}
