package middleware;

import server.Answer;

public class MessageVote extends MessageCreateDelete{

    private Answer answer;
    private int voto;

    public MessageVote(OperationCD operation, Answer answer, int voto){
        this.opcode = Opcode.Message_Vote;
        this.operation = operation;
        this.answer = answer;
        this.voto = voto;
    }

    public Answer getAnswer() {
        return answer;
    }

    public int getVoto() {
        return voto;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    @Override
    public Object getObject() {
        return getAnswer();
    }
}
