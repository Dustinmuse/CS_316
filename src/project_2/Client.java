package project_2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.*;
import java.util.Arrays;
public class Client {
    public static void main(String[] args) throws Exception{
        if(args.length != 2){
            System.out.println("Please specify <serverIP> and <serverPort>");
            return;
        }
        InetAddress serverIP = InetAddress.getByName(args[0]); //gets string and converts it to a IP address in a special format (get IP of user)
        int serverPort = Integer.parseInt(args[1]); //gets string and converts it to an Integer (get ports of user)

        String message = "";

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

        /* Convert byte array to unsigned integer manually
        long unsignedInt = ((long)(serverMessage[0] & 0xFF) << 24) |
                ((serverMessage[1] & 0xFF) << 16) |
                ((serverMessage[2] & 0xFF) << 8)  |
                (serverMessage[3] & 0xFF);
         */


        int buffer = ByteBuffer.wrap(serverMessage).getInt();
        long unsignedInt = Integer.toUnsignedLong(buffer);

        //unsignedInt -= 2208988800L; //subtract by 70 years

        System.out.println(unsignedInt);

        LocalDateTime epoch = LocalDateTime.of(1900, 1, 1, 0, 0, 0 );
        ZonedDateTime time = epoch.plusSeconds(unsignedInt).atZone(ZoneId.systemDefault());

        System.out.println(time);
        /*
        Instant timestamp = Instant.ofEpochSecond(unsignedInt);
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime time = ZonedDateTime.ofInstant(timestamp, zone);
        System.out.println(time);
         */
    }
}
