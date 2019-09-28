package Server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final String dirName = "uploads";
    private ServerSocket serverSocket;
    public static final String HOST = "localhost";
    private final int backlog = 50;

    public Server(int port)throws IOException {
        File dir = new File(dirName);
        if(!dir.exists() && !dir.mkdir()){
            System.out.println("Can't make an upload directory");
        }
        serverSocket = new ServerSocket(port, backlog, InetAddress.getByName(HOST));
    }

    public void work() throws IOException{
        while(true){
            Socket clientSocket = serverSocket.accept();
        }
    }
}
