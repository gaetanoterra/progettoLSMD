package server;

import java.io.IOException;

public class ServerStart {

    public static void main(String[] args) {
        if (args.length != 2){
            System.err.println("Please enter port number and backlog");
            return;
        }
        int portNumber, backlogLength;
        try {
            portNumber = Integer.parseInt(args[0]);
            backlogLength = Integer.parseInt(args[1]);
            if (portNumber > 65535 || portNumber < 2000){
                throw new Exception("Port number is not between 2000 and 65535");
            }
            Server server = new Server(portNumber, backlogLength);
            server.waitForConnection();
        }
        catch (NumberFormatException nfe) {
            System.out.println("Port number and/or backlog are not valid integers");
        }
        catch (IOException ioe) {
            System.out.println("A new server socket cannot be allocated. See stack trace for error details");
            ioe.printStackTrace();
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
