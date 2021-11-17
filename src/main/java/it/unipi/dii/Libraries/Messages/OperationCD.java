package it.unipi.dii.Libraries.Messages;

public enum OperationCD {
    Create  (0),
    Delete  (1),
    Check   (2);

    private final byte operation;
    OperationCD(int operation){
        this.operation = (byte)operation;
    }


    public byte getOperation() {
        return this.operation;
    }

    @Override
    public String toString() {
        return "OperationCD{" +
                "operation=" + ((operation == 0)? "CREATE":"DELETE") +
                '}';
    }
}
