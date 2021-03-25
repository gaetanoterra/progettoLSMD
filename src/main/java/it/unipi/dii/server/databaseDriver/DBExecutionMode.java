package it.unipi.dii.server.databaseDriver;

public enum DBExecutionMode {
    LOCAL("local"),
    REMOTE("remote") ,
    CLUSTER("cluster");
    private final String mode;

    DBExecutionMode(String m) {
        this.mode = (m.toLowerCase());
    }
}
