import java.io.*;
import java.net.URL;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        int side;
        System.out.println("Client = 0, Server = 1");
        Scanner scanner = new Scanner(System.in);
        side = scanner.nextInt();
        Server server = new Server();

        if (side == 0) {
            System.out.println("Enter IP address and port (address <ENTER> port <ENTER>)");
            String dest = "129.3.20.26"; //scanner.next();
            int udpPort = scanner.nextInt();
            Client client = new Client(dest, udpPort);
            System.out.println("Enter file path");
            //client.send(scanner.next());
            client.send("C:/Users/BAgunner300/Documents/csc445/NetworkingAssignment2/resources/Interior2.jpg");
        } else {
            getAddress();
            server.getPort();
            Packet receivedPacket = server.receive();
            System.out.println(receivedPacket.toString());
        }
    }

    private static void getAddress() {
        // Find public IP address
        String systemipaddress = "";
        try {
            URL url_name = new URL("http://bot.whatismyipaddress.com");

            BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));

            // reads system IPAddress
            systemipaddress = sc.readLine().trim();
        }
        catch (Exception e) {
            systemipaddress = "Cannot Execute Properly";
        }
        System.out.println("Public IP Address: " + systemipaddress);
    }
}
