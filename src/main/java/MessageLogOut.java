public class MessageLogOut extends Message{

    private String username;

    @Override
    public byte getOpcode() {
        return super.getOpcode();
    }

    public String getUsername() {
        return username;
    }
}
