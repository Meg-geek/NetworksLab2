package Server;


import java.io.IOException;

public class MainServer {
    public static final int SERVER_PORT = 7777;

    public static void main(String[] args){
        Server server = new Server();
        server.work(SERVER_PORT);
    }
}
