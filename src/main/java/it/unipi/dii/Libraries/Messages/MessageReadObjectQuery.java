package it.unipi.dii.Libraries.Messages;

public abstract class MessageReadObjectQuery extends Message {

    protected byte objectOpcode;

    public byte getObjectOpcode() {
        return this.objectOpcode;
    }
    public abstract Object getObject();
}
