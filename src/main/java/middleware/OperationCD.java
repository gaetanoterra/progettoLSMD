package middleware;

public enum OperationCD {
    Create  (0),
    Delete  (1);

    private final byte operation;
    OperationCD(int operation){
        this.operation = (byte)operation;
    }
}
