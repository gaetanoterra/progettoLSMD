package Libraries.Messages;

import java.io.Serializable;

public enum Parameter implements Serializable {
    Date  (0),
    Tags  (1),
    Username (2),
    Text (3);

    private final byte parameterType;
    Parameter(int parameterType){ this.parameterType = (byte) parameterType; }

    @Override
    public String toString() {
        switch (this.parameterType) {
            case 0:
                return "DATE";
            case 1:
                return "TAGS";
            case 2:
                return "USERNAME";
            default:
                return "TEXT";
        }
    }

    public byte getParameterType() {
        return this.parameterType;
    }



}
