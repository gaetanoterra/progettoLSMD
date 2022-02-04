package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Answer;

//classe messaggio, utilizzata per inviare una richiesta di creazione/eliminazione answer al server
public class MessageAnswer extends MessageCreateDelete{

    private Answer answer;

    public MessageAnswer(OperationCD operation, Answer answer){
        this.opcode = Opcode.Message_Answer;
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

    @Override
    public String toString() {
        return "Message{" +
                "ANSWER" +
                '}';
    }
}
