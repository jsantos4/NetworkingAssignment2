import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Server {
    private DatagramSocket udpSocket;

    public Server(){
        try {
            udpSocket = new DatagramSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receive(String filePath) {

        DatagramPacket packet = new DatagramPacket(new byte[516], 516);
        Packet ACK;
        ArrayList<Byte> fileData = new ArrayList<>();
        byte[] blockData;
        byte[] blockNumber = {0, 0};
        int dataSize = 516;
        int lpr = 0;

        try {
            System.out.println("Listening");
            udpSocket.receive(packet);
            udpSocket.setSoTimeout(5000);       //Once we get our first data packet, set a timeout so we can deal with dropped packets
            if (packet.getData()[1] == 0) {         //If packet was request, check protocol then send ACK with 0 block number
                ACK = new Packet(blockNumber);
                udpSocket.send(new DatagramPacket(ACK.getBytes(), 4, packet.getAddress(), packet.getPort()));
            }
            //Start loop for data packets
            do {
                lpr = Packet.getPacket(packet).getBlockNumber();

                try {
                    udpSocket.receive(packet);
                } catch (SocketTimeoutException timeout) {
                    System.out.println("Lost a packet, resending last ACK");
                    ACK = new Packet(blockNumber);
                    udpSocket.send(new DatagramPacket(ACK.getBytes(), 4, packet.getAddress(), packet.getPort()));
                }

                blockNumber[0] = packet.getData()[2];
                blockNumber[1] = packet.getData()[3];

                if (Packet.getPacket(packet).getBlockNumber() > lpr + 1) {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e ) {
                        e.printStackTrace();
                    }

                    continue;
                }

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
                System.out.println("New file created");
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
        System.out.println("File successfully written");
    }

    public void getPort() {
        System.out.println("UDP Port: " + udpSocket.getLocalPort());
    }
}
