package it.unipi.dii.Libraries.Messages;

import java.io.Serializable;

public enum Parameter implements Serializable {
    Username (0),
    Id(1),
    Text (2);

    private final byte parameterType;
    Parameter(int parameterType){ this.parameterType = (byte) parameterType; }

    @Override
    public String toString() {
        return switch (this.parameterType) {
            case 0 -> "USERNAME";
            case 1 -> "ID";
            default -> "TEXT";
        };
    }

    public byte getParameterType() {
        return this.parameterType;
    }



}
