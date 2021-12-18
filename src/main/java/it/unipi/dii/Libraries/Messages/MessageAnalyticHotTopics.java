package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.Post;
import it.unipi.dii.Libraries.User;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageAnalyticHotTopics extends Message{
    private HashMap<User, ArrayList<Pair<Post, Integer>>> map;

    public MessageAnalyticHotTopics(HashMap<User, ArrayList<Pair<Post, Integer>>> map){
        this.map = map;
        this.opcode = Opcode.Message_Analytic_Hot_Topics;
    }

    @Override
    public Opcode getOpcode() { return super.getOpcode(); }

    public HashMap<User, ArrayList<Pair<Post, Integer>>> getMap() { return map; }

    public void setMap(HashMap<User, ArrayList<Pair<Post, Integer>>> map) { this.map = map; }
}
