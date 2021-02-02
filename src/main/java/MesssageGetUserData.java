import java.io.Serializable;

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
