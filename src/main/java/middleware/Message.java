package middleware;

import java.io.Serializable;

public abstract class Message implements Serializable{

    protected Opcode opcode;

    public Opcode getOpcode() {
        return opcode;
    }
}
