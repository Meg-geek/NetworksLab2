package Server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements TCPServer{
    //public static final String DIR_NAME = "uploads";
    //public static final String HOST = "localhost";
    //public static final int SUCCESS = 0;
    //public static final int FAILURE = -1;
    //private final int backlog = 50;

    public Server(){
        File dir = new File(DIR_NAME);
        if(!dir.exists() && !dir.mkdir()){
            System.out.println("Can't make an upload directory");
        }
    }

    @Override
    public void work(int port){
        try(ServerSocket serverSocket = new ServerSocket(port, backlog, InetAddress.getByName(HOST))){
            int threadNumb = 0;
            while(true){
                Socket clientSocket = serverSocket.accept();
                Thread serverThread = new Thread(new FileLoadingThread(clientSocket, threadNumb));
                serverThread.start();
                threadNumb++;
            }
        } catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }
}
