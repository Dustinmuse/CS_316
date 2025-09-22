package echo_service_tcp;

import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class tcp_echo_server_file {
    public static void main(String[] args) throws Exception{
        ServerSocketChannel listenChannel = ServerSocketChannel.open(); //make socket on the server side (listens for client requests)

        //bind socket to specific channel
        listenChannel.bind(new InetSocketAddress(3000)); //port address that people need to use to get access to the server

        FileOutputStream fos = new FileOutputStream("test2.jpg");

        while (true){
            //listenChannel.accept(); //works with "connect method" on client side to make connection (if no client, it will wait forever)
            SocketChannel serveChannel = listenChannel.accept(); //since in while loop it will try to serve the next client on repeat

            ByteBuffer buffer = ByteBuffer.allocate(1024); //max size of buffer (1024)

            int bytesRead = 0;
            while((bytesRead = serveChannel.read(buffer)) != 1){
                buffer.flip(); //write mode/read mode (switches write mode to read mode)
                byte[] a = new byte[bytesRead]; //same size as we just read
                buffer.get(a); //new byte[] a holds all the info buffer had
                fos.write(a);
                buffer.clear();
            }

            fos.close();

            String replyMessage = "S";
            ByteBuffer replyBuffer = ByteBuffer.wrap(replyMessage.getBytes());
            serveChannel.write(replyBuffer); //sending bytes to channel

            serveChannel.close();

        }
    }
}

