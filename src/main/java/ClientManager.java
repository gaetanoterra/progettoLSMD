import java.io.*;
import java.net.*;

public class ClientManager extends Thread{

    private DBManager dbManager;
    private Socket socket;
    private String username;
    private ObjectOutputStream oos;
    private  ObjectInputStream ois;

    public ClientManager(Socket socket) throws IOException{
        this.socket = socket;
        ois = new ObjectInputStream(socket.getInputStream());
        oos = new ObjectOutputStream(socket.getOutputStream());
    }

    public void run(){
        try{
            while(true) {
                Message msg = (Message)ois.readObject();

                switch (msg.getOpcode()){
                    case Message_Ok:
                        break;

                    case Message_Fail:
                        break;

                    case Message_Login:
                        break;

                    case Message_Logout:
                        break;

                    case Message_Signup:
                        break;

                    case Message_Get_Experts:
                        break;

                    case Message_Create_Delete:
                        break;

                    case Message_Get_Post_Data:
                        break;

                    case Message_Get_Top_Users_Post:
                        break;
                }
            }

        }
        catch (Exception e) {e.printStackTrace();}
    }

    public Message receive() throws IOException, ClassNotFoundException {
        return (Message)ois.readObject();
    }

    public void send(Message messaggio) throws IOException {
        oos.writeObject(messaggio);
    }

}
