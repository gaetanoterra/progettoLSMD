import java.io.Serializable;

public abstract class MessageReadObjectQuery extends Message implements Serializable {

    private byte getObjectOpcode;

    public Object getObject() {
        return getObjectOpcode;
    }
}
