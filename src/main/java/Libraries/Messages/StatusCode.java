package Libraries.Messages;

public enum StatusCode {
    Message_Ok  (0),
    Message_Fail  (1);

    private final byte statusCode;
    StatusCode(int statusCode){
        this.statusCode = (byte) statusCode;
    }

    @Override
    public String toString() {
        return Byte.toString(this.statusCode);
    }

    public byte getStatusCode() {
        return this.statusCode;
    }
}
