package middleware;

public abstract class MessageCreateDelete extends Message {

    protected OperationCD operation;
    public OperationCD getOperation() {
        return operation;
    }
    public abstract Object getObject();
}
