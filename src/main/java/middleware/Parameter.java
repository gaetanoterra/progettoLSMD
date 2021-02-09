package middleware;

public enum Parameter{
    Date  (0),
    Tags  (1),
    Username (2),
    Text (3);

    private final byte operation;
    Parameter(int operation){ this.operation = (byte)operation; }
}
