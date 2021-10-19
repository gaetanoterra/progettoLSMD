package it.unipi.dii.Libraries.Messages;

public class MessageAnalyticMPTagsLocation extends Message {
    private String location;
    private int numTags;
    private String[] tags;

    public MessageAnalyticMPTagsLocation(String location, int numTags, String[] tags){
        this.opcode = Opcode.Message_Analytics_Most_Popular_Tags_Location;
        this.location = location;
        this.numTags = numTags;
        this.tags = tags;
    }

    @Override
    public Opcode getOpcode() { return super.getOpcode(); }

    public int getNumTags() { return numTags; }

    public String getLocation() { return location; }

    public String[] getTags() { return tags; }

    public void setLocation(String location) { this.location = location; }

    public void setNumTags(int numTags) { this.numTags = numTags; }

    public void setTags(String[] tags) { this.tags = tags; }
}
