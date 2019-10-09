package Client;

import Client.ClientExceptions.ClientException;
import Server.MainServer;
import Server.Server;

import java.io.IOException;
import java.net.UnknownHostException;
import Server.TCPServer;

public class ClientMain {
    public static void main(String[] args){
        Client client;
        try{
            client = new Client();
            client.sendFile("src/абв", TCPServer.HOST, MainServer.SERVER_PORT);
            //client.sendFile("src/image.jpeg", TCPServer.HOST, MainServer.SERVER_PORT);
        } catch(UnknownHostException ex){
            ex.printStackTrace();
        }
        catch(ClientException| IOException ex){
            ex.printStackTrace();
        }
    }
}
