package it.unipi.dii.Libraries.Messages;

import java.io.Serializable;

public enum Parameter implements Serializable {
    Date  (0),
    Tags  (1),
    Username (2),
    Id(3),
    Text (4);

    private final byte parameterType;
    Parameter(int parameterType){ this.parameterType = (byte) parameterType; }

    @Override
    public String toString() {
        return switch (this.parameterType) {
            case 0 -> "DATE";
            case 1 -> "TAGS";
            case 2 -> "USERNAME";
            case 3 -> "ID";
            default -> "TEXT";
        };
    }

    public byte getParameterType() {
        return this.parameterType;
    }



}
