package Client;

import Client.ClientExceptions.ClientException;
import Server.MainServer;
import Server.Server;

import java.io.IOException;
import java.net.UnknownHostException;
import Server.TCPServer;

public class ClientMain {
    public static void main(String[] args){
        Client client1;
        try{
            client1 = new Client();
            client1.sendFile("filePath", TCPServer.HOST, MainServer.SERVER_PORT);
        } catch(UnknownHostException ex){
            ex.printStackTrace();
        }
        catch(ClientException| IOException ex){
            ex.printStackTrace();
        }
    }
}
