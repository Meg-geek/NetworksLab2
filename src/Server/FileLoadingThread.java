package Server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

/*
* протокол - сначала передается число байт имени файла - int,
 * затем имя файла,
 * потом размер Файла - long,
 * затем файл
 * в дальнейшем сервер сравнивает, совпадает ли прочитанное число байт
 * с числом байт в файле клиента
 * и отправляет соответвенно константу, успешно ли завершилась передача
 * */

public class FileLoadingThread implements Runnable {
    private Socket clientSocket;
    private static final int DELAY_MILSEC = 3000;
    private static final int BUFSIZE = 1024;
    private AtomicLong readBytesAmount = new AtomicLong();
    private final int threadNumb;
    private long fileSize, readFileBytes;
    private File file;
    private Timer timer;

    public FileLoadingThread(Socket clientSocket, int threadNumb){
        this.clientSocket = clientSocket;
        this.threadNumb = threadNumb;
    }

    private void startTimer(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            private long prevBytesAmount = 0;
            private final double sec = DELAY_MILSEC / (double)1000;
            private final long startMilSec = new Date().getTime();
            @Override
            public void run() {
                long bytesAmount = readBytesAmount.get();
                double momentSpeed = (bytesAmount - prevBytesAmount) / sec;
                prevBytesAmount = bytesAmount;
                double allSec = ((new Date()).getTime() - startMilSec)/(double)1000;
                double allTimeSpeed = bytesAmount / allSec;
                System.out.println("Thread " + threadNumb + ". Moment speed " + momentSpeed + " bytes per sec");
                System.out.println("Thread " + threadNumb + ". Average speed " + allTimeSpeed + " bytes/sec");
            }
        }, DELAY_MILSEC, DELAY_MILSEC);
    }

    private String readFileName(DataInputStream in) throws IOException{
        int nameBytes = in.readInt(), readNameBytes = 0;
        byte[] buf = new byte[BUFSIZE];
        ByteArrayOutputStream fileNameStream = new ByteArrayOutputStream();
        readBytesAmount.addAndGet(4);
        while(readNameBytes < nameBytes){
            int length = in.read(buf, 0, nameBytes - readNameBytes);
            readNameBytes+= length;
            fileNameStream.write(buf, 0, length);
        }
        System.out.println("File name " + fileNameStream.toString(StandardCharsets.UTF_8));
        return fileNameStream.toString(StandardCharsets.UTF_8);
    }

    private void readFile(DataInputStream in, String fileName) throws IOException{
        file = new File(TCPServer.DIR_NAME + File.separator + fileName);
        if(!file.createNewFile()){
            //how?
            System.out.println("File " + fileName + " already exists on server");
            return; //?
            //throw ServerException("File " + fileName + "already exists on server"); ?
        }
        try(FileOutputStream fileWriter = new FileOutputStream(file)){
            fileSize = in.readLong();
            readBytesAmount.addAndGet(8);
            byte[] buf = new byte[BUFSIZE];
            while(readFileBytes < fileSize){
                int length = in.read(buf);
                readFileBytes+=length;
                readBytesAmount.addAndGet(length);
                fileWriter.write(buf, 0, length);
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
                     = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            readFile(in, readFileName(in));
            if(readFileBytes == fileSize){
                out.writeInt(TCPServer.SUCCESS);
            } else {
                out.writeInt(TCPServer.FAILURE);
                file.delete();
            }
        } catch(IOException ex) {
            timer.cancel();
            throw new RuntimeException(ex);
        } finally {
            timer.cancel();
        }
    }
}
