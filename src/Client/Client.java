package Client;

import Client.ClientExceptions.ClientException;
import Server.Server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class Client {
    private String filePath;
    private static final int MAX_FILENAME_LENGTH = 4096;
    private long fileLength;
    private Socket socket;

    public Client(String path, String serverName, int serverPort) throws ClientException, UnknownHostException,
            IOException {
        filePath = new String(path.getBytes(), StandardCharsets.UTF_8);
        checkFile();
        socket = new Socket(serverName, serverPort);
    }

    private void checkFile() throws ClientException{
        File file = new File(filePath);
        if(!file.exists()){
            throw new ClientException("File " + filePath + " doesn't exist");
        }
        if(!file.isFile()){
            throw new ClientException(filePath + " isn't a file");
        }
        if(file.getName().getBytes().length > MAX_FILENAME_LENGTH){
            throw new ClientException("Filename" + filePath + " is too long");
        }
        fileLength = file.length();
        double terabytes = (double)fileLength/Math.pow(1024, 4);
        if(terabytes > 1){
            throw new ClientException("File is too big");
        }
    }
}
