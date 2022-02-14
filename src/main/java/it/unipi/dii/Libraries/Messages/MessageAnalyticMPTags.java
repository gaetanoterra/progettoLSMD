package it.unipi.dii.Libraries.Messages;

import java.util.Map;

public class MessageAnalyticMPTags extends MessageReadObjectQuery {

    private Map<String, Integer> tags;

    public MessageAnalyticMPTags(Map<String,Integer> tags) {
        this.opcode = Opcode.Message_Analytics_Most_Popular_Tags;
        this.tags = tags;
    }

    @Override
    public Opcode getOpcode() { return super.getOpcode(); }

    @Override
    public Map<String, Integer> getObject() { return tags; }

    public void setTags(Map<String, Integer> tags) { this.tags = tags; }
}
