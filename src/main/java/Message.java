import java.io.Serializable;

public abstract class Message implements Serializable {

    private byte opcode;

    public byte getOpcode() {
        return opcode;
    }
}
