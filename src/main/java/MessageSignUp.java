import java.io.Serializable;

public class MessageSignUp extends Message {

    private User user;

    public MessageSignUp(User user){
        this.user = user;
    }

    @Override
    public Opcode getOpcode() {
        return super.getOpcode();
    }

    public User getUser() {
        return user;
    }
}
