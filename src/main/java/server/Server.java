package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {

    private ServerSocket serverSocket;
    private DBManager dbManager;
    private int portNumber;
    private int backlogLength;
    private List<ClientManager> clientManagersCreated;
    private final int numberThreadsToCheck = 50;
    private int counter = 0;

    public Server(int portNumber, int backlogLength) throws IOException{
        this.portNumber = portNumber;
        this.backlogLength = backlogLength;
        this.dbManager = new DBManager();
        this.serverSocket = new ServerSocket(this.portNumber, this.backlogLength);
        // Per mantenere traccia dei thread creati (e quindi delle connessioni con i client)
        // li conservo in una lista, visto che essendo i thread normali thread (e non daemon), vivranno
        // anche se il server dovesse crashare, ma senza le risorse necessarie per collegarsi al database,
        // visto che attualmente sono come dei singleton gestiti dal server e assegnati ai vari client, e quindi
        // cadendo il server con una eccezione libero pure le risorse legate ai database
        //TODO: Verificare se questa gestione dei thread ha senso nella classe
        this.clientManagersCreated = new ArrayList<>();
    }

    public void waitForConnection() throws Exception{
        //il server rimane in un ciclo in attesa di connessione
        System.out.println("Server listening on port " + this.portNumber);
        try {
            while (true) {
                //accetto le connessioni inviate da un'istanza di client.ClientServerManager
                Socket pendingRequestSocket = serverSocket.accept();
                System.out.println("Received new connection request");
                try {
                    //creo una nuova istanza di server.ClientManager
                    //TODO: se necessario creare una nuova istanza di DBManager per ogni client
                    ClientManager clientManager = new ClientManager(pendingRequestSocket, this.dbManager);
                    this.counter++;
                    if (this.counter > this.numberThreadsToCheck) {
                        this.removeDeadThreads();
                        this.counter = 0;
                    }
                    this.clientManagersCreated.add(clientManager);
                    clientManager.start();
                } catch (IOException ioe) {
                    pendingRequestSocket.close();
                    System.err.println(ioe.getMessage());
                }
            }
        }
        catch (Exception e) {
            for (ClientManager clientManager: this.clientManagersCreated) {
                clientManager.interrupt();
                clientManager.join();
            }
            this.clientManagersCreated.clear();
            this.dbManager.close();
            this.serverSocket.close();
            throw e;
        }

    }

    private void removeDeadThreads(){
        // Poiché vengono creati thread dal Server, e ogni thread rappresenta una connessione,
        // è possibile che una connessione cada in qualunque momento, e con questo il thread muore.
        // Se non controllo periodicamente quali thread sono vivi o meno, rischio di occupare molta memoria
        // mantenendo riferimenti a thread morti (e tra l'altro il garbage collector non eliminerà i thread
        // dalla memoria, visto che mantengo un riferimento in questa struttura
        this.clientManagersCreated.removeIf(clientManager -> !clientManager.isAlive());
    }


}
