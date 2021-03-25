package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Answer;

//classe messaggio, utilizzata per inviare una richiesta di creazione/eliminazione answer al server
public class MessageAnswer extends MessageCreateDelete{

    private Answer answer;
    private String postId;

    public MessageAnswer(OperationCD operation, Answer answer, String postId){
        this.opcode = Opcode.Message_Answer;
        this.operation = operation;
        this.answer = answer;
        this.postId = postId;
    }

    public Answer getAnswer() {
        return answer;
    }

    public String getPostId() {
        return postId;
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
