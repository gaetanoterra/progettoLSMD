package middleware;

public class MessageLogOut extends Message {

    private String username;

    public MessageLogOut(String username) {
        this.opcode = Opcode.Message_Logout;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
