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
            DatagramPacket packet = new DatagramPacket(reqPacket.getBytes(), reqPacket.getBytes().length, InetAddress.getLocalHost(), port);    //Change back to getByName(address)
            DatagramPacket response = new DatagramPacket(new byte[516], 516);

            //Send WRQ
            socket.send(packet);

            //Receive initial ACK or ERR
            socket.receive(response);
            System.out.println("ACK packet: " + Packet.getPacket(response).getBlockNumber());
            while (Packet.getPacket(response).getBlockNumber() != 0) {
                socket.send(packet);
                socket.receive(response);
            }

            //Send data while receiving ACKs in between each DATA packet
            int dataLeft = data.length;
            short blockNumber = 0;
            byte[] blockData = new byte[512];
            while ((dataLeft -= 512) >= 512) {
                System.arraycopy(data, blockNumber * 512, blockData, 0, 512);
                Packet nextData = new Packet(blockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
                DatagramPacket dataPacket = new DatagramPacket(nextData.getBytes(), nextData.getBytes().length, InetAddress.getLocalHost(), port);  //Change back to getByName(address)
                socket.send(dataPacket);
                System.out.println("Data packet: " + nextData.getBlockNumber());
                //Receive Ack
                socket.receive(response);
                System.out.println("ACK packet: " + Packet.getPacket(response).getBlockNumber());
                while (Packet.getPacket(response).getBlockNumber() != nextData.getBlockNumber()) {
                    socket.send(dataPacket);
                    socket.receive(response);
                }
                //if received packet opcode = 2 loop again, if opcode = 3 print error code and try again until response is ACK
                while (response.getData()[1] == (byte) 3) {
                    System.out.println("Error code: " + response.getData()[3]);
                    socket.send(dataPacket);
                    socket.receive(response);
                }

            }  //Once out of loop, next data packet has less than 512 bytes of data
            byte[] finalBlockData = new byte[dataLeft];
            System.arraycopy(data, blockNumber * 512, finalBlockData, 0, dataLeft);
            Packet nextData = new Packet(finalBlockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
            DatagramPacket finalPacket = new DatagramPacket(nextData.getBytes(), nextData.getBytes().length, InetAddress.getLocalHost(), port); //Change back to getByName(address)
            socket.send(finalPacket);
            socket.receive(response);
            System.out.println(Packet.getPacket(packet).toString());
            while (response.getData()[1] == (byte) 3) {
                System.out.println("Error code: " + response.getData()[3]);
                socket.send(finalPacket);
                socket.receive(response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
