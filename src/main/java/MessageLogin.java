import java.io.Serializable;

public class MessageLogin extends Message implements Serializable {

    private String username;
    private String password;

    @Override
    public byte getOpcode() {
        return super.getOpcode();
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }
}
