import java.io.Serializable;

public class MesssageGetUserData extends MessageReadObjectQuery implements Serializable {
    private User[] userList;

    @Override
    public byte getOpcode() {
        return super.getOpcode();
    }

    @Override
    public Object getObject() {
        return super.getObject();
    }
}
