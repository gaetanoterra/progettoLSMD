package middleware;

public enum StatusCode {
    Message_Ok  (0),
    Message_Fail  (1);

    private final byte code;
    StatusCode(int code){
        this.code = (byte)code;
    }
}
