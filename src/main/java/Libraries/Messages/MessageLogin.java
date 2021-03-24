package Libraries.Messages;

import Libraries.User;

//classe messaggio, utilizzata per inviare una richiesta di login al server (non è la stessa di MessagePost?)
public class MessageLogin extends Message {

    private User user;
    private StatusCode status;

    public MessageLogin(User user){
        this.opcode = Opcode.Message_Login;
        this.user = user;
    }

    public MessageLogin(User user, StatusCode status){
        this.opcode = Opcode.Message_Login;
        this.user = user;
        this.status = status;
    }

    public User getUser(){ return user; }

    public StatusCode getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "MessageLogin{" +
                "username=" + user +
                '}';
    }
}
