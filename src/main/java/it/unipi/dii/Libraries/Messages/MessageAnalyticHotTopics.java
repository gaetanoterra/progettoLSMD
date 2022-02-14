package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageAnalyticHotTopics extends MessageReadObjectQuery{

    private HashMap<User, ArrayList<Pair<Post, Integer>>> map;

    public MessageAnalyticHotTopics(HashMap<User, ArrayList<Pair<Post, Integer>>> map) {
        this.map = map;
        this.opcode = Opcode.Message_Analytic_Hot_Topics;
    }

    @Override
    public Opcode getOpcode() { return super.getOpcode(); }

    @Override
    public HashMap<User, ArrayList<Pair<Post, Integer>>>  getObject() { return map; }

    public void setMap(HashMap<User, ArrayList<Pair<Post, Integer>>> map) { this.map = map; }
}
