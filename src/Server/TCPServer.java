package Server;

import java.io.IOException;

public interface TCPServer {
    void work(int port) throws IOException;
}
