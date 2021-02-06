package middleware;

import server.User;

public class MessageUser extends MessageCreateDelete{

    private User user;

    public MessageUser(OperationCD operation, User user){
        this.opcode = Opcode.Message_User;
        this.operation = operation;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Object getObject() {
        return getUser();
    }
}
