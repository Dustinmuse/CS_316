package project_3;

// Project 3 - TCP File Service Server
// Dustin Muse & JD Otis

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class tcp_file_server {
    public static void main(String[] args) throws Exception{
        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(2013));

        while (true) {
            SocketChannel serveChannel = listenChannel.accept();
            ByteBuffer request = ByteBuffer.allocate(1024);
            int numBytes = serveChannel.read(request);
            request.flip();

            byte[] a = new byte[1]; // size of byte array should match the number of bytes in command
            request.get(a); // reads 1 byte out of buffer and places into a
            String command = new String(a);
            System.out.println("\nCommand received: "+command);

            switch (command){
                case "D": //Delete
                    byte[] b = new byte[request.remaining()]; //.remaining() gives an integer of remaining bytes
                    request.get(b);
                    String fileName = new String(b);
                    System.out.println("\nFile to delete: " + fileName);
                    File file = new File ("ServerFiles/" + fileName);

                    boolean success = false;
                    if(file.exists()){
                        success = file.delete(); //boolean determines whether deletion is successful or not
                    }

                    String status;
                    if(success){
                        status = "S";
                    } else {
                        status = "F";
                    }
                    ByteBuffer reply = ByteBuffer.wrap(status.getBytes());
                    serveChannel.write(reply);
                    serveChannel.close();
                    break;
                case "L": //List
                    File filePath = new File("ServerFiles/");
                    File[] files = filePath.listFiles();

                    // Send the int amount of files
                    int filesLength = files.length;

                    ByteBuffer lengthBuffer = ByteBuffer.allocate(4); // Send length as 4-byte integer
                    lengthBuffer.putInt(filesLength);
                    lengthBuffer.flip();

                    serveChannel.write(lengthBuffer);

                    // Send file names
                    for (int count = 0; count < filesLength; count++) {
                        String name = files[count].getName() + "\n"; // Get the file name
                        ByteBuffer LReply = ByteBuffer.wrap(name.getBytes());
                        serveChannel.write(LReply);
                    }
                    serveChannel.close();
                    break;
                case "R": //Rename
                    byte[] d = new byte[request.remaining()]; //.remaining() gives an integer of remaining bytes
                    request.get(d);
                    String[] fileNames = new String(d).split(",");

                    String oldFileName = fileNames[0].trim(); // Old file name
                    String newFileName = fileNames[1].trim(); // New file name

                    // System.out.println("Renaming file from: " + oldFileName + " to: " + newFileName); //debugging
                    File oldFile = new File("ServerFiles/" + oldFileName);
                    File newFile = new File("ServerFiles/" + newFileName);

                    boolean RSuccess = false;
                    if (oldFile.exists()) {
                        RSuccess = oldFile.renameTo(newFile); // Rename to the new file
                    }

                    String RStatus;
                    if(RSuccess){
                        RStatus = "S";
                    } else {
                        RStatus = "F";
                    }
                    ByteBuffer RReply = ByteBuffer.wrap(RStatus.getBytes());
                    serveChannel.write(RReply);
                    serveChannel.close();
                    break;

                case "U": //Upload
                    byte[] u = new byte[request.remaining()]; //.remaining() gives an integer of remaining bytes
                    request.get(u);
                    String UFileName = new String(u);
                    System.out.println("\nFile to receive: " + UFileName);

                    FileOutputStream fos = new FileOutputStream("ServerFiles/" + UFileName);

                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                    int bytesRead = 0;
                    while((bytesRead = serveChannel.read(buffer)) != -1) {
                        buffer.flip(); // Change mode of buffer from write mode to read mode
                        byte[] t = new byte[bytesRead];
                        buffer.get(t); // reads bytes out of buffer and places into a
                        fos.write(t);
                        buffer.clear();
                    }
                    fos.close();

                    File UFile = new File("ServerFiles/"+UFileName);
                    String UStatus;
                    if (UFile.exists()) {
                        UStatus = "S";
                    } else {
                        UStatus = "F";
                    }
                    ByteBuffer UReply = ByteBuffer.wrap(UStatus.getBytes());
                    serveChannel.write(UReply);
                    serveChannel.close();
                    break;

                case "W": //Download
                    byte[] WFileNameBytes = new byte[request.remaining()];
                    request.get(WFileNameBytes);
                    String WFileName = new String(WFileNameBytes);

                    File Wfile = new File("ServerFiles/" + WFileName);
                    if (Wfile.exists()) {
                        FileInputStream fis = new FileInputStream(Wfile);

                        ByteBuffer WBuffer = ByteBuffer.allocate(1024);
                        byte[] data = new byte[1024];
                        int WbytesRead = 0;
                        while ((WbytesRead = fis.read(data)) != -1) {
                            WBuffer.put(data, 0, WbytesRead);
                            WBuffer.flip();
                            serveChannel.write(WBuffer);
                            WBuffer.clear();
                        }
                        fis.close();
                    }
                    serveChannel.close();
                    break;
                default:
                    System.out.println("Invalid command.");
            }
        }
    }
}