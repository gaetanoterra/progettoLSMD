package middleware;

import client.*;
import server.*;

public class MesssageGetUserData extends MessageReadObjectQuery {
    private User[] userList;

    @Override
    public Opcode getOpcode() {
        return super.getOpcode();
    }

    @Override
    public Object getObject() {
        return super.getObject();
    }
}
