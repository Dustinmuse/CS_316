package echo_service_tcp;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/*
limitation 1: would be that it can only hold 1024 bytes maximum
limitation 2: would be that it can only serve 1 client at a time
*/
public class tcp_echo_server {
    public static void main(String[] args) throws Exception{
        ServerSocketChannel listenChannel = ServerSocketChannel.open(); //make socket on the server side (listens for client requests)

        //bind socket to specific channel
        listenChannel.bind(new InetSocketAddress(3000)); //port address that people need to use to get access to the server

        while (true){
            //listenChannel.accept(); //works with "connect method" on client side to make connection (if no client, it will wait forever)
            SocketChannel serveChannel = listenChannel.accept(); //since in while loop it will try to serve the next client on repeat

            ByteBuffer buffer = ByteBuffer.allocate(1024); //max size of buffer (1024)
            int bytesRead = serveChannel.read(buffer); //buffer is in write mode because it is currently writing to the buffer
            buffer.flip(); //write mode/read mode (switches write mode to read mode)
            byte[] a = new byte[bytesRead]; //same size as we just read
            buffer.get(a); //new byte[] a holds all the info buffer had
            String clientMessage = new String(a);
            System.out.println(clientMessage); //for debugging

            String replyMessage = clientMessage.toLowerCase();
            ByteBuffer replyBuffer = ByteBuffer.wrap(replyMessage.getBytes());
            serveChannel.write(replyBuffer); //sending bytes to channel

            serveChannel.close();

        }
    }
}
