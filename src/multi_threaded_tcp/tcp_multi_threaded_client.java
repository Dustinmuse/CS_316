package multi_threaded_tcp;
// Project 3 - TCP File Service Client
// Dustin Muse & JD Otis

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class tcp_multi_threaded_client {
    public static void main(String[] args) throws Exception { // Dustin IP: 10.222.24.143
        if (args.length != 2) {
            System.out.println("Please specify two arguments: <serverIP> and <serverPort>.");
            return;
        }

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);

        ExecutorService executor = Executors.newFixedThreadPool(4); // Thread pool with 4 threads

        String command;
        do{
            System.out.println("Please enter a command: \nD - Delete\nL - List\nR - Rename\nU - Upload\nW - Download\nQ - Quit");

            Scanner keyboard = new Scanner(System.in);
            command = keyboard.nextLine().toUpperCase();
            switch(command){ //decided to go with single letters :)
                case "D": //Delete
                    System.out.println("Enter the name of the file to be deleted: ");
                    String fileName = keyboard.nextLine();

                    executor.execute(new DeleteTask(serverIP, serverPort, command, fileName));
                    break;
                case "L": //List
                    executor.execute(new ListTask(serverIP, serverPort, command));
                    break;

                case "R": //Rename
                    System.out.println("Enter the name of the file to be renamed (example: test.txt): ");
                    String RFileName = keyboard.nextLine();
                    System.out.println("Enter the new name of the file (example: test1.txt): ");
                    String RFileNewName = keyboard.nextLine();

                    RFileName = RFileName + "," + RFileNewName;

                    executor.execute(new RenameTask(serverIP,serverPort, RFileName, command));
                    break;

                case "U": //Upload
                    System.out.println("Enter the name of the file to be uploaded: ");
                    String uploadFileName = keyboard.nextLine();

                    executor.execute(new UploadTask(serverIP, serverPort, uploadFileName));
                    break;
                case "W": //Download
                    System.out.println("Enter the name of the file to be downloaded: ");
                    String WFileName = keyboard.nextLine();

                    executor.execute(new DownloadTask(serverIP, serverPort, WFileName));
                    break;
                case "Q": //Quit
                    break;
                default:
                    System.out.println("Invalid input.");
            }
        } while(!command.equals("Q"));
    }

    static class UploadTask implements Runnable {
        private final String serverIP;
        private final int serverPort;
        private final String uploadFileName;

        public UploadTask(String serverIP, int serverPort, String uploadFileName) {
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.uploadFileName = uploadFileName;
        }
        public void run() {
            Upload(serverIP, serverPort, uploadFileName);
        }

        private static void Upload(String serverIP, int serverPort, String fileName) {
            try {
                File file = new File("ClientFiles/" + fileName);
                if (!file.exists()) {
                    System.out.println("File does not exist.");
                    return;
                }
                ByteBuffer request = ByteBuffer.wrap(("U" + fileName).getBytes());
                SocketChannel channel = SocketChannel.open();
                channel.connect(new InetSocketAddress(serverIP, serverPort));
                channel.write(request);

                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[1024];
                int bytesRead;
                Thread.sleep(500);
                while ((bytesRead = fis.read(data)) != -1) {
                    ByteBuffer buffer = ByteBuffer.wrap(data, 0, bytesRead);
                    channel.write(buffer);
                }
                fis.close();
                channel.shutdownOutput();

                ByteBuffer reply = ByteBuffer.allocate(1);
                channel.read(reply);
                channel.close();

                String response = new String(reply.array()).trim();
                if (response.equals("S")) {
                    System.out.println("File successfully uploaded.");
                } else {
                    System.out.println("Failed to upload file.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class DownloadTask implements Runnable{
        private final String serverIP;
        private final int serverPort;
        private final String fileName;
        public DownloadTask(String serverIP, int serverPort, String fileName){
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.fileName = fileName;
        }
        public void run(){
            Download(serverIP, serverPort, fileName);
        }
        private static void Download(String serverIP, int serverPort, String fileName){
            try {
                ByteBuffer request = ByteBuffer.wrap(("W" + fileName).getBytes());
                SocketChannel channel = SocketChannel.open();
                channel.connect(new InetSocketAddress(serverIP, serverPort));
                channel.write(request);
                channel.shutdownOutput();

                FileOutputStream fos = new FileOutputStream("ClientFiles/" + fileName);
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int bytesRead;

                while ((bytesRead = channel.read(buffer)) > 0) {
                    buffer.flip();
                    fos.write(buffer.array(), 0, bytesRead);
                    buffer.clear();
                }
                fos.close();
                channel.close();
                System.out.println("File downloaded successfully.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class RenameTask implements Runnable{
        private final String serverIP;
        private final int serverPort;
        private final String RfileName;
        private final String command;
        public RenameTask(String serverIP, int serverPort, String RfileName, String command){
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.RfileName = RfileName;
            this.command = command;
        }
        public void run(){
            Rename(serverIP, serverPort, RfileName, command);
        }
        public static void Rename(String serverIP, int serverPort, String RFileName, String command){
            try {
                ByteBuffer RRequest = ByteBuffer.wrap((command + RFileName).getBytes());
                SocketChannel RChannel = SocketChannel.open();

                RChannel.connect(new InetSocketAddress(serverIP, serverPort));
                RChannel.write(RRequest);
                RChannel.shutdownOutput(); //Tells server that the client is done sending

                ByteBuffer RReply = ByteBuffer.allocate(1);
                RChannel.read(RReply);
                RChannel.close();
                RReply.flip();
                byte[] Ra = new byte[1];
                RReply.get(Ra);
                String RCode = new String(Ra);
                if (RCode.equals("S")) {
                    System.out.println("File successfully renamed.");
                } else if (RCode.equals("F")) {
                    System.out.println("Failed to rename file.");
                } else {
                    System.out.println("Invalid server code received.");
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    static class ListTask implements Runnable{
        private final String serverIP;
        private final int serverPort;
        private final String command;
        public ListTask(String serverIP, int serverPort, String command){
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.command = command;
        }
        public void run(){
            List(serverIP, serverPort, command);
        }
        public static void List(String serverIP, int serverPort, String command){
            try {
                ByteBuffer LRequest = ByteBuffer.wrap((command).getBytes());
                SocketChannel LChannel = SocketChannel.open();

                LChannel.connect(new InetSocketAddress(serverIP, serverPort));
                LChannel.write(LRequest);
                LChannel.shutdownOutput();

                // Read the number of files (as a 4-byte integer)
                ByteBuffer replyLength = ByteBuffer.allocate(4); // Allocate enough space for an integer
                LChannel.read(replyLength);
                replyLength.flip();

                int fileListLength = replyLength.getInt();

                // Read each file name
                for (int i = 0; i < fileListLength; i++) {
                    ByteBuffer fileNameBuffer = ByteBuffer.allocate(256); // Adjust size as needed
                    LChannel.read(fileNameBuffer);
                    fileNameBuffer.flip();

                    byte[] nameBytes = new byte[fileNameBuffer.remaining()];
                    fileNameBuffer.get(nameBytes);
                    String LFileName = new String(nameBytes);
                    System.out.print(LFileName);
                }

                LChannel.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    static class DeleteTask implements Runnable{
        private final String serverIP;
        private final int serverPort;
        private final String command;
        private final String fileName;
        public DeleteTask(String serverIP, int serverPort, String command, String fileName){
            this.serverIP = serverIP;
            this.serverPort = serverPort;
            this.command = command;
            this.fileName = fileName;
        }
        public void run(){
            Delete(serverIP, serverPort, command, fileName);
        }
        public static void Delete(String serverIP, int serverPort, String command, String fileName){
            try {
                ByteBuffer request = ByteBuffer.wrap((command + fileName).getBytes());
                SocketChannel channel = SocketChannel.open();

                channel.connect(new InetSocketAddress(serverIP, serverPort));
                channel.write(request);
                channel.shutdownOutput(); //Tells server that the client is done sending

                ByteBuffer reply = ByteBuffer.allocate(1);
                channel.read(reply);
                channel.close();
                reply.flip();
                byte[] a = new byte[1];
                reply.get(a);
                String code = new String(a);
                if (code.equals("S")) {
                    System.out.println("File successfully deleted.");
                } else if (code.equals("F")) {
                    System.out.println("Failed to delete file.");
                } else {
                    System.out.println("Invalid server code received.");
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}