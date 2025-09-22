package echo_service_udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;

public class EchoClient {
    public static void main(String[] args) throws Exception{
        if(args.length != 2){
            System.out.println("Please specify <serverIP> and <serverPort>");
            return;
        }
        InetAddress serverIP = InetAddress.getByName(args[0]); //gets string and converts it to a IP address in a special format (get IP of user)
        int serverPort = Integer.parseInt(args[1]); //gets string and converts it to an Integer (get ports of user)

        while(true) { //MAKES IT INFINITLY LOOP
            Scanner keyboard = new Scanner(System.in); //reads from the keyboard (System.in is the keyboard input)
            String message = keyboard.nextLine();

            DatagramSocket socket = new DatagramSocket(); //assigns a random port #
            DatagramPacket request = new DatagramPacket(
                    message.getBytes(),
                    message.getBytes().length,
                    serverIP,
                    serverPort
            );
            socket.send(request); //give packet to socket (not really send)

            DatagramPacket reply = new DatagramPacket(
                    new byte[1024], //internal storage of the packet (1024)
                    1024 //length
            );
            socket.receive(reply);
            socket.close();

            byte[] serverMessage = Arrays.copyOf(
                    reply.getData(),
                    reply.getLength()
            );

            System.out.println(new String(serverMessage));
        } //while
    }
}
