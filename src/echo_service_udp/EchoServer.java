package echo_service_udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class EchoServer {
    public static void main(String[] args) throws Exception {
        //make request to OS for specific port # (will throw exception if the port is already been used
        DatagramSocket socket = new DatagramSocket(2020);
        DatagramPacket request = new DatagramPacket(
                new byte[1024],
                1024
        ); //maximum size of packet can be 1024

        while(true){
            socket.receive(request); //receive is a blocking call AKA: it will wait till it receives a packet (could pause program forever)
            InetAddress clientIP = request.getAddress(); //extracting the source from the IP header out of the whole packet
            int clientPort = request.getPort(); //extracting the port # from the UDP header
            //extract the data payload
            byte[] payload = Arrays.copyOf(
                    request.getData(),
                    request.getLength()
            );

            String clientMessage = new String(payload);
            System.out.println("Client message: " + clientMessage);
            String replyMessage = clientMessage.toUpperCase();
            //makes new message to send new reply
            DatagramPacket reply = new DatagramPacket(
                    replyMessage.getBytes(),
                    replyMessage.getBytes().length,
                    clientIP,
                    clientPort
            );

            socket.send(reply);
        } //while
    }// main
}// class
