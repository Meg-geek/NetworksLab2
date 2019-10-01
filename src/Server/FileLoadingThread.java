package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/*
* протокол - сначала передается число байт имени файла - int,
 * затем имя файла,
 * потом размер Файла - long,
 * затем файл*/

public class FileLoadingThread implements Runnable {
    private Socket clientSocket;
    private static final int DELAY_MILSEC = 3000;
    private static final int BUFSIZE = 256;
    private long recvBytesAmount;
    private DataInputStream in;
    private DataOutputStream out;

    public FileLoadingThread(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    private void startTimer(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            private int prevBytesAmount;
            @Override
            public void run() {

            }
        }, DELAY_MILSEC, DELAY_MILSEC);
    }

    private String readFileName() throws IOException{
        int nameBytes = in.readInt(), readBytes = 0;
        byte[] buf = new byte[BUFSIZE];
        StringBuilder fileName = new StringBuilder();
        recvBytesAmount+=4;
        while(readBytes < nameBytes){
            int length = in.read(buf);
            readBytes+= length;
            for(int i = 0; i < length; i++){
                fileName.append(buf[i]);
            }
        }
        return fileName.toString();
    }

    @Override
    public void run(){
        startTimer();
        try {
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            String fileName = readFileName();
            long fileSize = in.readLong(), readBytes = 0;
            recvBytesAmount+=8;
            while(readBytes < fileSize){

            }

        } catch(IOException ex) {

        } finally {

        }
    }
}
