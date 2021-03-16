package Libraries.Messages;

public class OpcodeNotValidException extends Exception{
    public OpcodeNotValidException(String message, Throwable cause){
        super(message, cause);
    }
    public OpcodeNotValidException(String message){
        super(message);
    }
    public  OpcodeNotValidException(){super();}
}
