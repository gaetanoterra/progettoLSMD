import java.io.Serializable;

public class MessageSignUp extends Message implements Serializable {

    private User user;

    @Override
    public byte getOpcode() {
        return super.getOpcode();
    }

    public User getUser() {
        return user;
    }
}
