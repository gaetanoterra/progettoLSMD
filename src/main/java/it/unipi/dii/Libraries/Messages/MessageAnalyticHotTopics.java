package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.User;
import javafx.util.Pair;
import java.util.Map;

public class MessageAnalyticHotTopics extends Message{
    private Map<User, Pair<String, Integer>[]> map;

    public MessageAnalyticHotTopics(Map<User, Pair<String, Integer>[]> map){
        this.map = map;
        this.opcode = Opcode.Message_Analytic_Hot_Topics;
    }

    @Override
    public Opcode getOpcode() { return super.getOpcode(); }

    public Map<User, Pair<String, Integer>[]> getMap() { return map; }

    public void setMap(Map<User, Pair<String, Integer>[]> map) { this.map = map; }
}
