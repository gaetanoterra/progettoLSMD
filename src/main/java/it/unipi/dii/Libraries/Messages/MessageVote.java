package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Answer;

//classe messaggio, utilizzata per inviare una richiesta di creazione/eliminazione voto al server
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

    @Override
    public String toString() {
        return "MessageVote{" +
                "opcode=" + opcode.name() +
                '}';
    }
}
