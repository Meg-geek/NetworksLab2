package Server;

import java.io.*;
import java.net.Socket;
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
    private static final int BUFSIZE = 256;
    private AtomicLong recvBytesAmount = new AtomicLong();
    private final int threadNumb;
    private long readBytes, fileSize;
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
                long bytesAmount = recvBytesAmount.get();
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
        int nameBytes = in.readInt(), readBytes = 0;
        byte[] buf = new byte[BUFSIZE];
        StringBuilder fileName = new StringBuilder();
        recvBytesAmount.addAndGet(4);
        while(readBytes < nameBytes){
            int length = in.read(buf);
            readBytes+= length;
            for(int i = 0; i < length; i++){
                fileName.append(buf[i]);
            }
        }
        return fileName.toString();
    }

    private void readFile(DataInputStream in, String fileName) throws IOException{
        file = new File(TCPServer.DIR_NAME + System.lineSeparator() + fileName);
        if(!file.createNewFile()){
            //how?
            System.out.println("File " + fileName + "already exists on server");
            return; //?
            //throw ServerException("File " + fileName + "already exists on server"); ?
        }
        try(FileOutputStream fileWriter = new FileOutputStream(file)){
            fileSize = in.readLong();
            recvBytesAmount.addAndGet(8);
            int bufSize = BUFSIZE; //bufSize ?
            byte[] buf = new byte[BUFSIZE];
            while(readBytes < fileSize){
                int length = in.read(buf);
                readBytes += length;
                recvBytesAmount.addAndGet(length);
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
        ) {
            readFile(in, readFileName(in));
        } catch(IOException ex) {
            timer.cancel();
            throw new RuntimeException(ex);
        } finally {
            try(DataOutputStream out
                        = new DataOutputStream(clientSocket.getOutputStream())){
                if(readBytes == fileSize){
                    out.writeInt(TCPServer.SUCCESS);
                } else {
                    out.writeInt(TCPServer.FAILURE);
                    file.delete();
                }
            } catch(IOException ex){
                timer.cancel();
                throw new RuntimeException(ex);
            } finally {
                timer.cancel();
            }
        }
    }
}
