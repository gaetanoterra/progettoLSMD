package middleware;

public enum OperationCD {
    Create  (0),
    Delete  (1);

    private final byte operation;
    OperationCD(int operation){
        this.operation = (byte)operation;
    }

    @Override
    public String toString() {
        return Byte.toString(this.operation);
    }

    public byte getOperation() {
        return this.operation;
    }
}
