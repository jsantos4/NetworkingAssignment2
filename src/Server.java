import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

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
        filePath = "/home/jsantos4/Documents/csc445/";
        ArrayList<Byte> fileData = new ArrayList<>();
        byte[] blockData;
        int dataSize = 516;
        try {
            System.out.println("Listening");
            do {
                udpSocket.setSoTimeout(30000);
                udpSocket.receive(packet);
                if (packet.getData()[1] == 0) {         //If packet was request, set file path, send ACK with 0 block number
                    byte[] blockNumber = {0, 0};
                    filePath += "piccy.jpg";
                    Packet ACK = new Packet(blockNumber);
                    udpSocket.send(new DatagramPacket(ACK.getBytes(), 4, packet.getAddress(), packet.getPort()));
                } else if (packet.getData()[1] == 1) {  //If packet was data, get block data, add it to file data, send ACK with appropriate block number
                    byte[] blockNumber = {packet.getData()[2], packet.getData()[3]};
                    dataSize = packet.getLength();
                    blockData =  new byte[dataSize - 4];    //Size of data in packet, ie. packet - 4 bytes for opcode and block number
                    System.arraycopy(packet.getData(), 4, blockData, 0, dataSize - 4);      //Copy data from packet starting after opcode and block number
                    for (byte b : blockData) {
                        fileData.add(b);
                    }
                    Packet ACK = new Packet(blockNumber);
                    udpSocket.send(new DatagramPacket(ACK.getBytes(), 4, packet.getAddress(), packet.getPort()));
                }
            } while (dataSize == 516);

            writeFile(fileData, filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }

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
