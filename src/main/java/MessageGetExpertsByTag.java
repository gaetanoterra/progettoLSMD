import java.util.ArrayList;

public class MessageGetExpertsByTag extends Message{

    private String tag;
    private ArrayList usersList;

    public ArrayList getUsersList() {
        return usersList;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public byte getOpcode() {
        return super.getOpcode();
    }
}
