package Client;

import Client.ClientExceptions.ClientException;
import Server.TCPServer;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client implements TCPClient{
    private String filePath;
    private static final int MAX_FILENAME_LENGTH = 4096;
    private static final int BUF_SIZE = 1024;
    private long fileLength;
    private File file;

    public void sendFile(String path, String serverName, int serverPort) throws ClientException, IOException{
       // filePath = new String(path.getBytes(), StandardCharsets.UTF_8);
        filePath = path;
        checkFile();
        try(Socket socket = new Socket(serverName, serverPort);
            DataInputStream in
                    = new DataInputStream(socket.getInputStream());
            DataOutputStream out
                    = new DataOutputStream(socket.getOutputStream())
        ){
            sendFile(out);
            checkDataTransfer(in);
        }
    }

    /*
     * протокол - сначала передается число байт имени файла - int,
     * затем имя файла,
     * потом размер Файла - long,
     * затем файл
     * в дальнейшем сервер сравнивает, совпадает ли прочитанное число байт
     * с числом байт в файле клиента
     * и отправляет соответвенно константу, успешно ли завершилась передача
     * */

    private void checkDataTransfer(DataInputStream in) throws IOException{
        if(in.readInt() == TCPServer.SUCCESS){
            System.out.println("File " + file.getName() + " was sent successfully");
        } else {
            System.out.println("Failure in sending file " + file.getName());
        }
    }

    private void sendFile(DataOutputStream out) throws IOException{
        try(FileInputStream fileInputStream
                    = new FileInputStream(file)){
            byte[] nameBytes = file.getName().getBytes(StandardCharsets.UTF_8);
            out.writeInt(nameBytes.length);
            out.write(nameBytes);
            out.writeLong(file.length());
            byte[] buf = new byte[BUF_SIZE];
            int bytesRead = fileInputStream.read(buf);
            while(bytesRead != -1){
                out.write(buf, 0, bytesRead);
                bytesRead = fileInputStream.read(buf);
            }
        }
    }

    private void checkFile() throws ClientException{
        file = new File(filePath);
        if(!file.exists()){
            throw new ClientException("File " + filePath + " doesn't exist");
        }
        if(!file.isFile()){
            throw new ClientException(filePath + " isn't a file");
        }
        if(file.getName().getBytes(StandardCharsets.UTF_8).length > MAX_FILENAME_LENGTH){
            throw new ClientException("Filename" + filePath + " is too long");
        }
        fileLength = file.length();
        double terabytes = (double)fileLength/Math.pow(1024, 4);
        if(terabytes > 1){
            throw new ClientException("File is too big");
        }
    }
}
