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
        try {
            System.out.println("Listening");
            udpSocket.receive(packet);

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
