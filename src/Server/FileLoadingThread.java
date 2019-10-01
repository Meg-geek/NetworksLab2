package Server;

import java.io.*;
import java.net.Socket;
import java.util.Date;
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
    private volatile long recvBytesAmount;
    private final int threadNumb;
    private long readBytes, fileSize;
    private File file;
   // private DataInputStream in;
    //private DataOutputStream out;

    public FileLoadingThread(Socket clientSocket, int threadNumb){
        this.clientSocket = clientSocket;
        this.threadNumb = threadNumb;
    }

    private void startTimer(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            private long prevBytesAmount = 0;
            private final double sec = DELAY_MILSEC / 1000;
            private final long startMilSec = new Date().getTime();
            @Override
            public void run() {
                long bytesAmount = recvBytesAmount;
                double momentSpeed = (bytesAmount - prevBytesAmount) / sec;
                prevBytesAmount = bytesAmount;
                double allSec = ((new Date()).getTime() - startMilSec)/1000;
                double allTimeSpeed = recvBytesAmount / allSec;
                System.out.println("Thread " + threadNumb + ". Moment speed " + momentSpeed + " bytes per sec");
                System.out.println("Thread " + threadNumb + ". Average speed " + allTimeSpeed + " bytes/sec");
            }
        }, DELAY_MILSEC, DELAY_MILSEC);
    }

    private String readFileName(DataInputStream in, DataOutputStream out) throws IOException{
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

    private void readFile(DataInputStream in, DataOutputStream out, String fileName) throws IOException{
        file = new File(Server.DIR_NAME + System.lineSeparator() + fileName);
        if(!file.createNewFile()){
            //how?
            System.out.println("File " + fileName + "already exists on server");
            return; //?
            //throw ServerException("File " + fileName + "already exists on server"); ?
        }
        try(FileOutputStream fileWriter = new FileOutputStream(file);){
            fileSize = in.readLong();
            recvBytesAmount+=8;
            int bufSize = BUFSIZE; //bufSize ?
            byte[] buf = new byte[BUFSIZE];
            while(readBytes < fileSize){
                int length = in.read(buf);
                readBytes += length;
                recvBytesAmount+=length;
                fileWriter.write(buf, 0, length);
                //нужен ли контроль буфера?
                if(length == bufSize){
                    buf = new byte[bufSize * 2];
                }
                if(length < BUFSIZE){
                    buf = new byte[BUFSIZE];
                }
                //??
            }
        } catch(IOException ex){
            throw ex;
        }
    }

    @Override
    public void run(){
        startTimer();
        try (DataInputStream in
                     = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream out
                = new DataOutputStream(clientSocket.getOutputStream());) {
            readFile(in, out, readFileName(in, out));
        } catch(IOException ex) {
            ex.printStackTrace();
        } finally {
            if(readBytes == fileSize){
                System.out.println("Successful operation");
            } else {
                System.out.println("Operation wasn't successful");
                file.delete();
            }
        }
    }
}
