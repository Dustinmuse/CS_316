package multi_threaded_tcp;

// Project 3 - TCP File Service Server
// Dustin Muse & JD Otis

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class tcp_multi_threaded_server {
    public static void main(String[] args) throws Exception {
        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(2013));

        ExecutorService threadPool = Executors.newFixedThreadPool(4);

        while (true) {
            SocketChannel serveChannel = listenChannel.accept();
            threadPool.execute(new ClientHandler(serveChannel));
        }
    }
}

class ClientHandler implements Runnable {
    private SocketChannel serveChannel;

    public ClientHandler(SocketChannel serveChannel) {
        this.serveChannel = serveChannel;
    }

    @Override
    public void run() {
        try {
            ByteBuffer request = ByteBuffer.allocate(1024);
            int numBytes = serveChannel.read(request);
            request.flip();

            byte[] a = new byte[1];
            request.get(a);
            String command = new String(a);
            System.out.println("\nCommand received: " + command);

            switch (command) {
                case "D": // Delete
                    handleDelete(request);
                    break;
                case "L": // List
                    handleList();
                    break;
                case "R": // Rename
                    handleRename(request);
                    break;
                case "U": // Upload
                    handleUpload(request);
                    break;
                case "W": // Download
                    handleDownload(request);
                    break;
                default:
                    System.out.println("Invalid command.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                serveChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleDelete(ByteBuffer request) throws Exception {
        byte[] b = new byte[request.remaining()];
        request.get(b);
        String fileName = new String(b);
        System.out.println("\nFile to delete: " + fileName);
        File file = new File("ServerFiles/" + fileName);

        boolean success = file.exists() && file.delete();

        String status;
        if (success) {
            status = "S";
        } else {
            status = "F";
        }

        ByteBuffer reply = ByteBuffer.wrap(status.getBytes());
        serveChannel.write(reply);
    }

    private void handleList() throws Exception {
        File filePath = new File("ServerFiles/");
        File[] files = filePath.listFiles();

        int filesLength = files != null ? files.length : 0;
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(filesLength);
        lengthBuffer.flip();
        serveChannel.write(lengthBuffer);

        if (files != null) {
            for (File file : files) {
                ByteBuffer fileNameBuffer = ByteBuffer.wrap((file.getName() + "\n").getBytes());
                serveChannel.write(fileNameBuffer);
            }
        }
    }

    private void handleRename(ByteBuffer request) throws Exception {
        byte[] d = new byte[request.remaining()];
        request.get(d);
        String[] fileNames = new String(d).split(",");

        String oldFileName = fileNames[0].trim();
        String newFileName = fileNames[1].trim();

        File oldFile = new File("ServerFiles/" + oldFileName);
        File newFile = new File("ServerFiles/" + newFileName);

        boolean success = oldFile.exists() && oldFile.renameTo(newFile);

        String status;
        if (success) {
            status = "S";
        } else {
            status = "F";
        }

        ByteBuffer reply = ByteBuffer.wrap(status.getBytes());
        serveChannel.write(reply);
    }

    private void handleUpload(ByteBuffer request) throws Exception {
        byte[] u = new byte[request.remaining()];
        request.get(u);
        String fileName = new String(u);

        FileOutputStream fos = new FileOutputStream("ServerFiles/" + fileName);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (serveChannel.read(buffer) > 0) {
            buffer.flip();
            fos.write(buffer.array(), 0, buffer.limit());
            buffer.clear();
        }
        fos.close();

        File file = new File("ServerFiles/" + fileName);
        String status;
        if (file.exists()) {
            status = "S";
        } else {
            status = "F";
        }

        ByteBuffer reply = ByteBuffer.wrap(status.getBytes());
        serveChannel.write(reply);
    }

    private void handleDownload(ByteBuffer request) throws Exception {
        byte[] fileNameBytes = new byte[request.remaining()];
        request.get(fileNameBytes);
        String fileName = new String(fileNameBytes);

        File file = new File("ServerFiles/" + fileName);
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(data)) != -1) {
                buffer.put(data, 0, bytesRead);
                buffer.flip();
                serveChannel.write(buffer);
                buffer.clear();
            }
            fis.close();
        }
    }
}
