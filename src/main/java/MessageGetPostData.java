import java.io.Serializable;

public class MessageGetPostData extends MessageReadObjectQuery implements Serializable {

    private Post[] post;

    @Override
    public byte getOpcode() {
        return super.getOpcode();
    }

    @Override
    public Object getObject() {
        return super.getObject();
    }
}
