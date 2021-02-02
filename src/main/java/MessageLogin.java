import java.io.Serializable;

public class MessageLogin extends Message {

    private Opcode opcode;
    private String username;
    private String password;

    public MessageLogin(Opcode opcode, String username, String password){
        this.opcode = opcode;
        this.username = username;
        this.password = password;
    }
    @Override
    public Opcode getOpcode() {
        return super.getOpcode();
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }
}
