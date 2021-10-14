package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.User;

//classe messaggio, utilizzata per inviare una richiesta di registrazione al server
public class MessageSignUp extends Message {

    private User user;
    private StatusCode status;

    public MessageSignUp(User user, StatusCode status){
        this.opcode = Opcode.Message_Signup;
        this.user = user;
        this.status = status;
    }

    public MessageSignUp(User user) {
        this(user, null);
    }

    public MessageSignUp(StatusCode status) {
        this(null, status);
    }

    public User getUser() {
        return user;
    }
    public StatusCode getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "MessageSignUp{" +
                "user=" + user.getDisplayName() +
                '}';
    }
}
