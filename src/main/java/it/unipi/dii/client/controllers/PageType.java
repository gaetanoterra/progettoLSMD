package it.unipi.dii.client.controllers;

public enum PageType {
    POSTSEARCHINTERFACE(0),
    SIGN_IN (1),
    SIGN_UP (2),
    PROFILE_INTERFACE (3),
    WRITE (4),
    ANALYSIS_INTERFACE (5),
    MESSAGE (6),
    READ_POST (7),
    CREATE_ANSWER(8),
    FULLPOST(9),
    EXTERNAL_PROFILE(10); //profilo di un utente che non sono io
    private final int pagetype;

    PageType (int pagetype){
        this.pagetype = pagetype;
    }
}
