package project_3;

// Project 3 - TCP File Service Client
// Dustin Muse & JD Otis

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class tcp_file_client {
    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            System.out.println("Please specify two arguments: <serverIP> and <serverPort>.");
        }
        int serverPort = Integer.parseInt(args[1]);

        String command;
        do{
            System.out.println("Please enter a command: \nD - Delete\nL - List\nR - Rename\nU - Upload\nW - Download\nQ - Quit");

            Scanner keyboard = new Scanner(System.in);
            command = keyboard.nextLine().toUpperCase();
            switch(command){ //decided to go with single letters :)
                case "D": //Delete
                    System.out.println("Enter the name of the file to be deleted: ");
                    String fileName = keyboard.nextLine();

                    ByteBuffer request = ByteBuffer.wrap((command+fileName).getBytes());
                    SocketChannel channel = SocketChannel.open();

                    channel.connect(new InetSocketAddress(args[0], serverPort));
                    channel.write(request);
                    channel.shutdownOutput(); //Tells server that the client is done sending

                    ByteBuffer reply = ByteBuffer.allocate(1);
                    channel.read(reply);
                    channel.close();
                    reply.flip();
                    byte[] a = new byte[1];
                    reply.get(a);
                    String code = new String(a);
                    if(code.equals("S")){
                        System.out.println("File successfully deleted.");
                    } else if(code.equals("F")) {
                        System.out.println("Failed to delete file.");
                    } else{
                        System.out.println("Invalid server code received.");
                    }
                    break;
                case "L": //List
                    ByteBuffer LRequest = ByteBuffer.wrap((command).getBytes());
                    SocketChannel LChannel = SocketChannel.open();

                    LChannel.connect(new InetSocketAddress(args[0], serverPort));
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
                    break;

                case "R": //Rename
                    System.out.println("Enter the name of the file to be renamed (example: test.txt): ");
                    String RFileName = keyboard.nextLine();
                    System.out.println("Enter the new name of the file (example: test1.txt): ");
                    String RFileNewName = keyboard.nextLine();

                    RFileName = RFileName + "," + RFileNewName;

                    ByteBuffer RRequest = ByteBuffer.wrap((command+RFileName).getBytes());
                    SocketChannel RChannel = SocketChannel.open();

                    RChannel.connect(new InetSocketAddress(args[0], serverPort));
                    RChannel.write(RRequest);
                    RChannel.shutdownOutput(); //Tells server that the client is done sending

                    ByteBuffer RReply = ByteBuffer.allocate(1);
                    RChannel.read(RReply);
                    RChannel.close();
                    RReply.flip();
                    byte[] Ra = new byte[1];
                    RReply.get(Ra);
                    String RCode = new String(Ra);
                    if(RCode.equals("S")){
                        System.out.println("File successfully renamed.");
                    } else if(RCode.equals("F")) {
                        System.out.println("Failed to rename file.");
                    } else{
                        System.out.println("Invalid server code received.");
                    }
                    break;

                case "U": //Upload
                    System.out.println("Enter the name of the file to be uploaded: ");
                    String UFileName = keyboard.nextLine();
                    File fileToUpload = new File("ClientFiles/"+UFileName);
                    if (!fileToUpload.exists()) {
                        System.out.println("File does not exist.");
                        break;
                    }
                    FileInputStream fis = new FileInputStream("ClientFiles/"+UFileName);
                    ByteBuffer URequest= ByteBuffer.wrap((command+UFileName).getBytes());
                    SocketChannel UChannel = SocketChannel.open();

                    UChannel.connect(new InetSocketAddress(args[0], serverPort));
                    UChannel.write(URequest); // Send command + file name

                    byte[] data = new byte[1024];
                    int bytesRead = 0;
                    Thread.sleep(500);

                    while((bytesRead=fis.read(data)) != -1) {
                        ByteBuffer buffer = ByteBuffer.wrap(data, 0, bytesRead);
                        UChannel.write(buffer);
                    }

                    UChannel.shutdownOutput();
                    fis.close();

                    ByteBuffer UReply = ByteBuffer.allocate(1);
                    UChannel.read(UReply);
                    UChannel.close();
                    UReply.flip();
                    byte[] Ua = new byte[1];
                    UReply.get(Ua);
                    String UCode = new String(Ua);
                    if(UCode.equals("S")){
                        System.out.println("File successfully uploaded.");
                    } else if(UCode.equals("F")) {
                        System.out.println("Failed to upload file.");
                    } else{
                        System.out.println("Invalid server code received.");
                    }
                    break;
                case "W": //Download
                    System.out.println("Enter the name of the file to be downloaded: ");
                    String WFileName = keyboard.nextLine();

                    SocketChannel WChannel = SocketChannel.open();
                    WChannel.connect(new InetSocketAddress(args[0], serverPort));

                    ByteBuffer WRequest = ByteBuffer.wrap((command + WFileName).getBytes());
                    WChannel.write(WRequest);
                    WChannel.shutdownOutput();

                    FileOutputStream fos = new FileOutputStream("ClientFiles/" + WFileName);
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int WbytesRead = 0;

                    while ((WbytesRead = WChannel.read(buffer)) > 0) {
                        buffer.flip();
                        fos.write(buffer.array(), 0, WbytesRead);
                        buffer.clear();
                    }

                    fos.close();
                    WChannel.close();
                    break;
                case "Q": //Quit
                    break;
                default:
                    System.out.println("Invalid input.");
            }
        } while(!command.equals("Q"));
    }
}