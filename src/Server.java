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
        recieve();
    }

    private void recieve() {
        try {
            System.out.println("Listening");

            byte[] nameBytes = new byte[4];
            DatagramPacket fileName = new DatagramPacket(nameBytes, 4);
            udpSocket.receive(fileName);

            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getPort() {
        System.out.println("TCP Port: " + tcpSocket.getLocalPort());
        System.out.println("UDP Port: " + udpSocket.getLocalPort());
    }
}
