package server;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2){
            System.err.println("Please enter port number and backlog");
            return;
        }
        int p, b;
        try
        {
            p = Integer.parseInt(args[0]);
            b = Integer.parseInt(args[1]);
            if(p > 65535 || p < 2000){
                System.err.println("port number not valid");
                return;
            }
        }
        catch(Exception e)
        {
            System.out.println("Port number and/or backlog not valid");
            return;
        }
        try {
            Server server = new Server(p, b);
            server.waitForConnection();
        }catch (IOException ioe){ioe.printStackTrace();}

    }

}
