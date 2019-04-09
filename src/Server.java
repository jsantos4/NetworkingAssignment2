import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class Server {
    private ServerSocket tcpSocket;
    private DatagramSocket udpSocket;

    public Server(){
        try {
            tcpSocket = new ServerSocket(0);
            udpSocket = new DatagramSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Packet receive() {
        DatagramPacket packet = new DatagramPacket(new byte[516], 516);
        int dataSize = 516;
        try {
            System.out.println("Listening");
            do {
                udpSocket.receive(packet);
                if (packet.getData()[1] == 0) {
                    byte[] blockNumber = {0, 0};
                    Packet ACK = new Packet(blockNumber);
                    udpSocket.send(new DatagramPacket(ACK.getBytes(), 4, packet.getAddress(), packet.getPort()));

                } else if (packet.getData()[1] == 1) {
                    byte[] blockNumber = {packet.getData()[2], packet.getData()[3]};
                    Packet ACK = new Packet(blockNumber);
                    udpSocket.send(new DatagramPacket(ACK.getBytes(), 4, packet.getAddress(), packet.getPort()));
                    dataSize = packet.getData().length;

                }
                System.out.println((int) packet.getData()[3]);
            } while (dataSize == 516);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Packet.getPacket(packet);

    }

    public void getPort() {
        System.out.println("TCP Port: " + tcpSocket.getLocalPort());
        System.out.println("UDP Port: " + udpSocket.getLocalPort());
    }
}
