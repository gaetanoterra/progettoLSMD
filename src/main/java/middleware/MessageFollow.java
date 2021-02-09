package middleware;

import server.User;

//classe messaggio, utilizzata per inviare una richiesta di creazione/eliminazione follow al server
public class MessageFollow extends MessageCreateDelete{

    private User user;

    public MessageFollow(Opcode opcode, OperationCD operation, User user){
        this.opcode = Opcode.Message_Follow;
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
}
