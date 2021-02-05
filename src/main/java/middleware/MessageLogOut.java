package middleware;

import client.*;
import server.*;

public class MessageLogOut extends Message {

    private String username;

    @Override
    public Opcode getOpcode() {
        return super.getOpcode();
    }

    public String getUsername() {
        return username;
    }
}
