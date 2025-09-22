package echo_service_tcp;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class tcp_echo_client {
    public static void main(String[] args) throws Exception{
        if(args.length != 2){
            System.out.println("Please specify <serverIP> and <serverPort>");
            return;
        }
        int serverPort = Integer.parseInt(args[1]);

        Scanner keyboard = new Scanner(System.in);
        String message = keyboard.nextLine(); //message to send to server

        SocketChannel channel = SocketChannel.open();
        //the channel is establish when connect() returns
        channel.connect(new InetSocketAddress(args[0], serverPort)); //go through 3-way handshake and establish the connection (connect to this ip and port #)

        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes()); //makes message turn into bytes for the server to understand
        channel.write(buffer); //writing message to server

        ByteBuffer bufferReply = ByteBuffer.allocate(1024); //max size of buffer (1024)
        int bytesRead = channel.read(bufferReply);
        channel.close();
        bufferReply.flip(); //write mode/read mode (switches write mode to read mode)
        byte[] a = new byte[bytesRead]; //same size as we just read
        bufferReply.get(a); //new byte[] a holds all the info buffer had
        String serverMessage = new String(a);
        System.out.println(serverMessage);
    }
}
