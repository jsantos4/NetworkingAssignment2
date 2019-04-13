import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.concurrent.ThreadLocalRandom;

public class Client {
    private static DatagramSocket socket;
    private String address;
    private int port, windowSize = 10;
    private boolean addressProtocol, packetProtocol, dropProtocol;

    public Client(String address, int port, boolean[] options) {
        this.address = address;
        this.port = port;
        addressProtocol = options[0];
        packetProtocol = options[1];
        dropProtocol = options[2];
    }

    public void send(String fileName) {

        if (packetProtocol) {
            sendSliding(fileName);
            return;
        }

        try {
            socket = new DatagramSocket();
            File file = new File(fileName);
            byte[] data = Files.readAllBytes(file.toPath());
            Packet reqPacket = new Packet(fileName);
            DatagramPacket response = new DatagramPacket(new byte[516], 516);
            DatagramPacket packetForSend;

            if (addressProtocol) {
                packetForSend = new DatagramPacket(reqPacket.getBytes(), 516, Inet6Address.getByName(address), port);
            } else {
                packetForSend = new DatagramPacket(reqPacket.getBytes(), 516, Inet4Address.getByName(address), port);
            }

            //Send WRQ
            socket.send(packetForSend);

            //Receive initial ACK or ERR
            socket.receive(response);
            System.out.println("ACK packet: " + Packet.getPacket(response).getBlockNumber());
            while (Packet.getPacket(response).getBlockNumber() != 0) {
                socket.send(packetForSend);
                socket.receive(response);
            }

            //Send data while receiving ACKs in between each DATA packet
            int dataLeft = data.length;
            short blockNumber = 0;
            byte[] blockData = new byte[512];
            int dropLottery = 101;
            Packet nextData;
            if (dropProtocol)
                dropLottery = ThreadLocalRandom.current().nextInt(100);
            do {
                System.arraycopy(data, blockNumber * 512, blockData, 0, 512);
                nextData = new Packet(blockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
                packetForSend.setData(nextData.getBytes());
                if (ThreadLocalRandom.current().nextInt(100) != dropLottery)
                    socket.send(packetForSend);
                System.out.println("Data packet: " + nextData.getBlockNumber());
                //Receive Ack
                socket.receive(response);
                //Subtract data from dataLeft
                dataLeft -= 512;
                System.out.println("ACK packet: " + Packet.getPacket(response).getBlockNumber());
                while (Packet.getPacket(response).getBlockNumber() != nextData.getBlockNumber()) {
                    socket.send(packetForSend);
                    socket.receive(response);
                }
                //if received packet opcode = 2 loop again, if opcode = 3 print error code and try again until response is ACK
                while (response.getData()[1] == (byte) 3) {
                    System.out.println("Error code: " + response.getData()[3]);
                    socket.send(packetForSend);
                    socket.receive(response);
                }

            } while (dataLeft >= 512); //Once out of loop, next data packet has less than 512 bytes of data

            byte[] finalBlockData = new byte[dataLeft];
            System.arraycopy(data, blockNumber * 512, finalBlockData, 0, dataLeft);
            nextData = new Packet(finalBlockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
            DatagramPacket finalPacket = new DatagramPacket(nextData.getBytes(), nextData.getBytes().length, packetForSend.getAddress(), port);
            socket.send(finalPacket);
            System.out.println("Data packet: " + nextData.getBlockNumber());
            socket.receive(response);
            System.out.println("ACK packet: " + Packet.getPacket(response).getBlockNumber());

            while (response.getData()[1] == (byte) 3) {
                System.out.println("Error code: " + response.getData()[3]);
                socket.send(finalPacket);
                socket.receive(response);
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendSliding(String fileName) {
        try {
            socket = new DatagramSocket();
            File file = new File(fileName);
            byte[] data = Files.readAllBytes(file.toPath());
            Packet reqPacket = new Packet(fileName);
            DatagramPacket response = new DatagramPacket(new byte[516], 516);
            DatagramPacket packetForSend;

            if (addressProtocol) {
                packetForSend = new DatagramPacket(reqPacket.getBytes(), reqPacket.getBytes().length, Inet6Address.getByName(address), port);
            } else {
                packetForSend = new DatagramPacket(reqPacket.getBytes(), reqPacket.getBytes().length, Inet4Address.getByName(address), port);
            }

            //Send WRQ
            socket.send(packetForSend);

            //Receive initial ACK or ERR
            socket.receive(response);
            System.out.println("ACK packet: " + Packet.getPacket(response).getBlockNumber());
            while (Packet.getPacket(response).getBlockNumber() != 0) {
                socket.send(packetForSend);
                socket.receive(response);
            }

            socket.setSoTimeout(5000);
            int dataLeft = data.length;
            short blockNumber = 0;
            int lastAckReceived;
            byte[] blockData = new byte[512];
            Packet nextData;

            int dropLottery = 101;
            if (dropProtocol)
                dropLottery = ThreadLocalRandom.current().nextInt(100);

            //Send initial window
            for (int i = 0; i < windowSize; i++) {
                System.arraycopy(data, blockNumber * 512, blockData, 0, 512);
                nextData = new Packet(blockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
                packetForSend.setData(nextData.getBytes());
                socket.send(packetForSend);

            }

            System.out.println("Initial window sent");

            socket.receive(response);

            while (dataLeft > 512) {
                System.arraycopy(data, blockNumber * 512, blockData, 0, 512);
                lastAckReceived = Packet.getPacket(response).getBlockNumber();

                nextData = new Packet(blockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
                packetForSend.setData(nextData.getBytes());

                if (ThreadLocalRandom.current().nextInt(100) != dropLottery)
                    socket.send(packetForSend);


                try {
                    socket.receive(response);
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout, going back to: " + lastAckReceived);
                    blockNumber = (short) lastAckReceived;
                    continue;
                }

                dataLeft = data.length - (blockNumber * 512);
            }

            byte[] finalBlockData = new byte[dataLeft];
            System.arraycopy(data, blockNumber * 512, finalBlockData, 0, dataLeft);
            nextData = new Packet(finalBlockData, ByteBuffer.allocate(2).putShort(++blockNumber).array());
            DatagramPacket finalPacket = new DatagramPacket(nextData.getBytes(), nextData.getBytes().length, packetForSend.getAddress(), port);
            socket.send(finalPacket);
            System.out.println("Data packet: " + nextData.getBlockNumber());
            socket.receive(response);
            System.out.println("ACK packet: " + Packet.getPacket(response).getBlockNumber());

            socket.close();

        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }
}
