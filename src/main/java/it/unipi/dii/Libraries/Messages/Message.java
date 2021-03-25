package it.unipi.dii.Libraries.Messages;

import java.io.Serializable;

public abstract class Message implements Serializable{

    protected Opcode opcode;

    public Opcode getOpcode() {
        return opcode;
    }


}
