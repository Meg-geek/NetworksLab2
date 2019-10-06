package Server;

import java.io.IOException;

public interface TCPServer {
    String DIR_NAME = "uploads";
    String HOST = "localhost";
    int SUCCESS = 0;
    int FAILURE = -2;
    int backlog = 50;
    void work(int port);
}
