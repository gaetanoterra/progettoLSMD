import java.io.Serializable;

public class MessageLogOut extends Message {

    private String username;

    @Override
    public Opcode getOpcode() {
        return super.getOpcode();
    }

    public String getUsername() {
        return username;
    }
}
