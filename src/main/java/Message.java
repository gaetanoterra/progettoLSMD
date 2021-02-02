import java.io.Serializable;

public abstract class Message implements Serializable{

    private Opcode opcode;

    public Opcode getOpcode() {
        return opcode;
    }
}
