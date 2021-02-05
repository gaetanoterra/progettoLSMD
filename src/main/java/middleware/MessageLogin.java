package middleware;

import client.*;
import server.*;

public class MessageLogin extends Message {

    private Opcode opcode;
    private User user;

    public MessageLogin(Opcode opcode, User user){
        this.opcode = opcode;
        this.user = user;
    }

    public MessageLogin(Opcode opcode){
        this.opcode = opcode;
    }
    @Override
    public Opcode getOpcode() {
        return opcode;
    }

    public User getUser(){ return user; }
}
