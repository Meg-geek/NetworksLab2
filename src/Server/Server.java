package Server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements TCPServer{
    public static final String DIR_NAME = "uploads";
    private ServerSocket serverSocket;
    public static final String HOST = "localhost";
    private final int backlog = 50;

    public Server(int port) throws IOException {
        File dir = new File(DIR_NAME);
        if(!dir.exists() && !dir.mkdir()){
            System.out.println("Can't make an upload directory");
        }
        serverSocket = new ServerSocket(port, backlog, InetAddress.getByName(HOST));
    }

    //how to handle exceptions properly?
    public void work() throws IOException{
        int threadNumb = 0;
        while(true){
            Socket clientSocket = serverSocket.accept();
            Thread serverThread = new Thread(new FileLoadingThread(clientSocket, threadNumb));
            serverThread.start();
            threadNumb++;
        }
    }
}
