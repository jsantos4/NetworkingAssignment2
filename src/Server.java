import java.io.*;
import java.net.*;
import java.util.ArrayList;

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

    public void receive(String filePath) throws SocketException{
        DatagramPacket packet = new DatagramPacket(new byte[516], 516);
        Packet ACK;
        ArrayList<Byte> fileData = new ArrayList<>();
        byte[] blockData;
        byte[] blockNumber = {0, 0};
        int dataSize = 516;
        try {
            System.out.println("Listening");
            udpSocket.receive(packet);
            udpSocket.setSoTimeout(2000);       //Once we get our first data packet, set a timeout so we can deal with dropped packets
            if (packet.getData()[1] == 0) {         //If packet was request, set file path, send ACK with 0 block number
                filePath += "piccy.jpg";
                ACK = new Packet(blockNumber);
                udpSocket.send(new DatagramPacket(ACK.getBytes(), 4, packet.getAddress(), packet.getPort()));
            }
            //Start loop for data packets
            do {
                try {
                    udpSocket.receive(packet);
                } catch (SocketTimeoutException e) {
                    System.out.println("Lost a packet, resending last ACK");
                    ACK = new Packet(blockNumber);
                    udpSocket.send(new DatagramPacket(ACK.getBytes(), 4, packet.getAddress(), packet.getPort()));
                    continue;                       //If we time out, resend the last ACK and reiterate
                }

                blockNumber[0] = packet.getData()[2];
                blockNumber[1] = packet.getData()[3];
                dataSize = packet.getLength();
                blockData =  new byte[dataSize - 4];    //Size of data in packet, ie. packet - 4 bytes for opcode and block number
                System.arraycopy(packet.getData(), 4, blockData, 0, dataSize - 4);      //Copy data from packet starting after opcode and block number
                for (byte b : blockData) {
                    fileData.add(b);
                }

                ACK = new Packet(blockNumber);
                udpSocket.send(new DatagramPacket(ACK.getBytes(), 4, packet.getAddress(), packet.getPort()));
            } while (dataSize == 516);

            writeFile(fileData, filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }

        udpSocket.close();
    }

    private void writeFile(ArrayList<Byte> fileData, String filePath) {
        try {
            File file = new File(filePath);

            //Create file if it doesn't exist
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    System.out.println("Invalid path");
                    e.printStackTrace();
                }
            }
            //Convert array list to array so we can write it into the file
            byte[] bytesToWrite = new byte[fileData.size()];
            for (int i = 0; i < fileData.size(); i++) {
                bytesToWrite[i] = fileData.get(i);
            }

            FileOutputStream outputStream = new FileOutputStream(file);

            outputStream.write(bytesToWrite);
            outputStream.flush();
            outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    public void getPort() {
        System.out.println("TCP Port: " + tcpSocket.getLocalPort());
        System.out.println("UDP Port: " + udpSocket.getLocalPort());
    }
}
