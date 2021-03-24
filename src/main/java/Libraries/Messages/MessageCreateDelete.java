package Libraries.Messages;

public abstract class MessageCreateDelete extends Message {

    protected OperationCD operation;
    public OperationCD getOperation() {
        return operation;
    }
    public abstract Object getObject();

    @Override
    public String toString() {
        return "MessageCreateDelete{" +
                "CREATE_DELETE"+
                '}';
    }
}
