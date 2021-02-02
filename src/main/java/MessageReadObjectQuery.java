import java.io.Serializable;

public abstract class MessageReadObjectQuery extends Message {

    private byte getObjectOpcode;

    public Object getObject() {
        return getObjectOpcode;
    }
}
