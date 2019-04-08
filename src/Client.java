import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class Client {
    private static DatagramSocket socket;
    private String address;
    private int port;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void send(String fileName) {
        try {
            socket = new DatagramSocket();
            File file = new File(fileName);
            byte[] data = Files.readAllBytes(file.toPath());

            Packet reqPacket = new Packet(fileName);
            DatagramPacket packet = new DatagramPacket(reqPacket.getBytes(), reqPacket.getBytes().length, InetAddress.getByName(address), port);

            //Send WRQ
            socket.send(packet);

            //Receive initial ACK or ERR


            //Send data while receiving ACKs in between each DATA packet
            packet.setLength(516);
            int dataLeft = data.length;
            int blockNumber = 0;
            byte[] blockData = new byte[512];
            while ((dataLeft -= 512) >= 512) {
                System.arraycopy(data, blockNumber * 512, blockData, 0, 512);
                Packet nextData = new Packet(blockData, ByteBuffer.allocate(2).putInt(++blockNumber).array());
                packet.setData(nextData.getBytes());
                socket.send(packet);

                //Receive Ack
                //socket.receive();
                //if received packet opcode = 2 loop again, if opcode = 3 figure some shit out cuz we gotta errur

            }  //Once out of loop, next data packet has less than 512 bytes of data
            System.arraycopy(data, blockNumber * 512, blockData, 0, dataLeft);
            Packet nextData = new Packet(blockData, ByteBuffer.allocate(2).putInt(++blockNumber).array());
            packet.setData(nextData.getBytes());
            socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
