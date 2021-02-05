package middleware;

import client.*;
import server.*;

public abstract class MessageReadObjectQuery extends Message {

    private byte getObjectOpcode;

    public Object getObject() {
        return getObjectOpcode;
    }
}
