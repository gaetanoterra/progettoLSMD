package middleware;

import client.*;
import server.*;

public class MessageSignUp extends Message {

    private Opcode opcode;
    private User user;

    public MessageSignUp(Opcode opcode, User user){
        this.opcode = opcode;
        this.user = user;
    }

    @Override
    public Opcode getOpcode() {
        return super.getOpcode();
    }

    public User getUser() {
        return user;
    }
}
