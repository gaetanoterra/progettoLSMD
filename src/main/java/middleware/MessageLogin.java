package middleware;

import server.*;

public class MessageLogin extends Message {

    private User user;

    public MessageLogin(User user){
        this.opcode = Opcode.Message_Login;
        this.user = user;
    }

    public User getUser(){ return user; }
}
