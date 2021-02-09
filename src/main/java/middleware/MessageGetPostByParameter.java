package middleware;

import org.graalvm.compiler.nodes.calc.IntegerDivRemNode;

import server.Post;

public class MessageGetPostByParameter extends Message{

    private Parameter parameter;
    private String value;
    private Post[] post;

    public MessageGetPostByParameter(Parameter parameter, String value, Post[] post){
        this.opcode = Opcode.Message_Get_Post;
        this.parameter = parameter;
        this.value = value;
        this.post = post;
    }

    public Parameter getParameter() { return parameter; }

    public String getValue() { return value; }
}
