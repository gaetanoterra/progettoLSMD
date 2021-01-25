public class MessageSignUp extends Message{

    private User user;

    @Override
    public byte getOpcode() {
        return super.getOpcode();
    }

    public User getUser() {
        return user;
    }
}
