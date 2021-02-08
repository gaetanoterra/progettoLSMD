package middleware;

import server.*;

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
}
