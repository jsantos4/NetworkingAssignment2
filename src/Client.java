import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    private static DatagramSocket socket;
    private String address;
    private int port;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public void send(String file) {
        try {
            socket = new DatagramSocket(port, InetAddress.getByName(address));
            byte[] bytes = file.getBytes();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
