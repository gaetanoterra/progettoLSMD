package it.unipi.dii.client.controllers;

public enum PageType {
    POSTSEARCH_INTERFACE(0),
    SIGN_IN (1),
    SIGN_UP (2),
    PROFILE_INTERFACE (3),
    WRITE (4),
    ANALYSIS_INTERFACE (5),
    MESSAGE (6),
    READ_POST (7),
    CREATE_ANSWER(8),
    FULL_POST(9);
    private final int pagetype;

    PageType (int pagetype){
        this.pagetype = pagetype;
    }
}
