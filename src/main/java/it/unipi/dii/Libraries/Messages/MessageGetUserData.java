package it.unipi.dii.Libraries.Messages;

import it.unipi.dii.Libraries.User;
import it.unipi.dii.client.controllers.PageType;

//classe messaggio, utilizzata per inviare una richiesta dati utente al server
public class MessageGetUserData extends MessageReadObjectQuery {
    private User user;
    private boolean profileType;  //il profile type indica se i dati che vogliamo sono i nostri o di un utente di cui vogliamo vedere il profilo
    private PageType pageType;

    //usato per la risposta dal server
    public MessageGetUserData(User user, boolean profileType, PageType pageType){
        this.opcode = Opcode.Message_Get_User_Data;
        this.user = user;
        this.profileType = profileType;
        this.pageType = pageType;
    }

    @Override
    public User getObject() {
        return this.user;
    }

    @Override
    public String toString() {
        return "MessageGetUserData{" +
                "opcode=" + opcode.name() +
                '}';
    }

    public boolean getProfileType() {
        return profileType;
    }

    public PageType getPageType() { return pageType; }
}
