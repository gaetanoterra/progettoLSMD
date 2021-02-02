import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;
    private int portNumber;
    private int backlog;

    public void waitForConnection(){
        try(ServerSocket serverSocket = new ServerSocket(portNumber)){
            //il server rimane in un ciclo in attesa di connessione
            while(true){
                //accetto le connessioni inviate da un'istanza di ClientServerManager
                try(Socket s = serverSocket.accept()){

                    System.out.println("nuova richiesta di connessione ricevuta");

                    //creo una nuova istanza di ClientManager
                    //ClientManager clientManager = new ClientManager();
                    //clientManager.run();

                }catch(IOException e){System.err.println(e.getMessage());}
            }
        }catch(IOException ioe){System.err.println(ioe.getMessage());}
    }

    public static void main(String[] args){

    }
}
