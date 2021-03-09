package middleware;

public enum Opcode {
    //save some space, from int to byte (4 to 1 byte)
    Message_Login   (0),
    Message_Signup  (1),
    Message_Logout  (2),
    Message_Get_User_Data   (3),
    Message_Get_Post_Data   (4),
    Message_Get_Post    (5),
    Message_Get_Top_Users_Posts (6),
    Message_Get_Experts (7),
    Message_Answer  (8),
    Message_User    (9),
    Message_Follow  (10),
    Message_Vote    (11),
    Message_Post    (12),
    Message_Update_User_data    (13);
    private final byte opcode;

    Opcode(int opcode){
        this.opcode = (byte)opcode;
    }

    @Override
    public String toString() {
        return Byte.toString(this.opcode);
    }

    public byte getOpcode() {
        return this.opcode;
    }

}
