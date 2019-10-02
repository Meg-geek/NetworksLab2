package Client;

import Client.ClientExceptions.ClientException;

import java.io.IOException;

public interface TCPClient {
    void sendFile(String path, String serverName, int serverPort) throws ClientException, IOException;
}
