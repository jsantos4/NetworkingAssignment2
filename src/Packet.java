import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class Packet {

    private byte[] packetBytes;
    private byte opCode; // type 0 = request, 1 = data, 2 = acknowledgement, 3 = error
    private final String MODE = "octet"; // Mode field of request packet

    //WRQ packet
    public Packet(String fileName) {
        this.opCode = 0;

        this.packetBytes = new byte[4 + fileName.length() + MODE.length()];  // Length of packet will be: opcode (2 bytes), plus 2 "0" bytes, plus length of MODE (always "octet"), plus length of file name

        int packetByte = 0;
        this.packetBytes[packetByte] = 0;
        this.packetBytes[++packetByte] = this.opCode;

        System.arraycopy(fileName.getBytes(), 0, packetBytes, ++packetByte, fileName.length());

        this.packetBytes[packetByte += fileName.length()] = 0;

        System.arraycopy(MODE.getBytes(), 0, packetBytes, ++packetByte, MODE.length());

        this.packetBytes[packetByte += MODE.length()] = 0;

    }

    //DATA packet
    public Packet(byte[] data, byte[] blockNumber) {
        this.opCode = 1;

        this.packetBytes = new byte[4 + data.length];  // Length of packet will be: opcode (2 bytes), plus block number (2 bytes), plus however much data is in that block
        int packetByte = 0;
        this.packetBytes[packetByte] = 0;
        this.packetBytes[++packetByte] = this.opCode;

        System.arraycopy(blockNumber, 0, data, ++packetByte, blockNumber.length);

        System.arraycopy(data, 0, packetBytes, ++packetByte, data.length);

    }

    //ACK packet
    public Packet(byte[] blockNumber) {
        this.opCode = 2;

        this.packetBytes = new byte[4];  // Length of packet will be: opcode (2 bytes), plus block number (2 bytes)
        this.packetBytes[0] = 0;
        this.packetBytes[1] = this.opCode;

        System.arraycopy(blockNumber, 0, this.packetBytes, 2, blockNumber.length);
    }


    public static Packet getPacket(DatagramPacket packet) {
        byte[] data = packet.getData();

        if (data[1] == 0) {                             // If packet is WRQ
            StringBuffer buffer = new StringBuffer();
            int dataByte = 1;  // Start after opcode
            while ((int) data[++dataByte] != 0) {
                buffer.append((char)data[dataByte]);
            }

            return new Packet(buffer.toString());

        } else if (data[1] == 1) {                       // If packet is DATA
            byte[] blockData = new byte[data.length - 4];
            System.arraycopy(data, 4, blockData, 0, data.length - 4);
            byte[] blockNumber = {data[2], data[3]};

            return new Packet(blockData, blockNumber);

        } else if (data[1] == 2) {                      // If packet is ACK
            byte[] blockNumber = {data[2], data[3]};

            return new Packet(blockNumber);
        }

        return null;
    }

    public byte[] getBytes() {
        return packetBytes;
    }

    public int getBlockNumber() {
        return ((packetBytes[2] & 0xff) << 8) | (packetBytes[3] & 0xff);
    }

    public String toString() {
        String s = "opcode: " + opCode + ", MODE: " + MODE + ", data: ";
        for (byte b : packetBytes){
            s += b + " ";
        }

        return s;
    }
}
