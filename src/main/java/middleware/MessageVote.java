package middleware;

import server.Answer;

public class MessageVote extends MessageCreateDelete{

    private Answer answer;

    public MessageVote(OperationCD operation, Answer answer){
        this.opcode = Opcode.Message_Vote;
        this.operation = operation;
        this.answer = answer;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    @Override
    public Object getObject() {
        return getAnswer();
    }
}
