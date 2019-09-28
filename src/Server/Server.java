package Server;

import java.io.File;

public class Server {
    private int port;
    private final String dirName = "uploads";

    public Server(int port){
        this.port = port;
        File dir = new File(dirName);
        if(!dir.exists() && !dir.mkdir()){
            System.out.println("Can't make an upload directory");
        }
    }
}
