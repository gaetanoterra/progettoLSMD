package Libraries.Messages;

import Libraries.User;

//classe messaggio, utilizzata per inviare una richiesta di registrazione al server
public class MessageSignUp extends Message {

    private User user;
    private StatusCode status;

    //usato dal client
    public MessageSignUp(User user) {
        this(user, null);
    }
    //usato dal server
    public MessageSignUp(StatusCode status) {
        this(null, status);
    }

    public MessageSignUp(User user, StatusCode status){
        this.opcode = Opcode.Message_Signup;
        this.user = user;
        this.status = status;
    }

    public User getUser() {
        return user;
    }
    public StatusCode getStatus() {
        return status;
    }
}
