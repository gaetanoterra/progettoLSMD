package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {

    private ServerSocket serverSocket;
    DBManager dbManager;
    private int portNumber;
    private int backlog;

    public Server(int pn, int bl) throws IOException{
        this.portNumber = pn;
        this.backlog = bl;
        this.dbManager = new DBManager();
        this.serverSocket = new ServerSocket(pn);
    }

    public void waitForConnection(){
        //il server rimane in un ciclo in attesa di connessione
        System.out.println("Server listening on port " + this.portNumber);
        while(true){
            //accetto le connessioni inviate da un'istanza di client.ClientServerManager
            Socket pendingRequestSocket;
            try {
                pendingRequestSocket = serverSocket.accept();
                System.out.println("Received new connection request");

                //creo una nuova istanza di server.ClientManager
                ClientManager clientManager = new ClientManager(pendingRequestSocket, this.dbManager);
                clientManager.run();
            }catch(IOException ioe){System.err.println(ioe.getMessage());}
        }

    }


}
