package Libraries.Messages;

public enum Parameter{
    Date  (0),
    Tags  (1),
    Username (2),
    Text (3);

    private final byte parameterType;
    Parameter(int parameterType){ this.parameterType = (byte) parameterType; }

    @Override
    public String toString() {
        return Byte.toString(this.parameterType);
    }

    public byte getParameterType() {
        return this.parameterType;
    }
}
