import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    public void receive() {
        DatagramPacket packet = new DatagramPacket(new byte[516], 516);
        String uploadFolder = "/home/jsantos4/Documents/csc445/assignment2/";
        File file = new File(uploadFolder);
        byte[] blockData;
        OutputStream outputStream;
        int dataSize = 516;
        try {
            System.out.println("Listening");
            do {
                udpSocket.receive(packet);
                if (packet.getData()[1] == 0) {
                    byte[] blockNumber = {0, 0};
                    file = new File(uploadFolder + Packet.getPacket(packet).getFileName());
                    if (!file.exists())
                        file.createNewFile();
                    Packet ACK = new Packet(blockNumber);
                    udpSocket.send(new DatagramPacket(ACK.getBytes(), 4, packet.getAddress(), packet.getPort()));

                } else if (packet.getData()[1] == 1) {
                    byte[] blockNumber = {packet.getData()[2], packet.getData()[3]};
                    dataSize = packet.getLength();
                    blockData =  new byte[dataSize];
                    System.arraycopy(packet.getData(), 4, blockData, 0, dataSize);      //Copy data from packet starting after opcode and block number
                    if (file.exists()) {
                        outputStream = new FileOutputStream(file);
                        outputStream.write(blockData);
                        outputStream.flush();
                    }
                    Packet ACK = new Packet(blockNumber);
                    udpSocket.send(new DatagramPacket(ACK.getBytes(), 4, packet.getAddress(), packet.getPort()));
                }
            } while (dataSize == 516);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getPort() {
        System.out.println("TCP Port: " + tcpSocket.getLocalPort());
        System.out.println("UDP Port: " + udpSocket.getLocalPort());
    }
}
