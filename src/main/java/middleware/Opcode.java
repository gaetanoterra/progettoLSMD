package middleware;

public enum Opcode {
    //save some space, from int to byte (4 to 1 byte)
    Message_Login   (0),
    Message_Signup  (1),
    Message_Logout  (2),
    Message_Read_Object_Query   (3),
    Message_Get_User_Data   (4),
    Message_Get_Post_Data   (5),
    Message_Get_Post    (6),
    Message_Get_Top_Users_Posts (7),
    Message_Get_Experts (8),
    Message_Answer  (9),
    Message_User    (10),
    Message_Follow  (11),
    Message_Vote    (12),
    Message_Post    (13);
    private final byte opcode;


    Opcode(int opcode){
        this.opcode = (byte)opcode;
    }
}
