import java.io.Serializable;

public class MessageLogOut extends Message implements Serializable {

    private String username;

    @Override
    public byte getOpcode() {
        return super.getOpcode();
    }

    public String getUsername() {
        return username;
    }
}
