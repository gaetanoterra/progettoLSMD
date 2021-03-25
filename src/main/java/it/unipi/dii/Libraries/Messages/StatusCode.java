package it.unipi.dii.Libraries.Messages;

public enum StatusCode {
    Message_Ok   (0),
    Message_Fail (1);

    private final byte statusCode;
    StatusCode(int statusCode){
        this.statusCode = (byte) statusCode;
    }

    @Override
    public String toString() {
        return (this.statusCode==0)? "OK":"FAIL";
    }

    public byte getStatusCode() {
        return this.statusCode;
    }
}
