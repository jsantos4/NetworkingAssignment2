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
            String dest = "68.127.58.79"; //scanner.next();
            int udpPort = scanner.nextInt();
            Client client = new Client(dest, udpPort);
            System.out.println("Enter file path");
            client.send("Documents/csc445/assignment2/NetworkingAssignment2/resources/Interior2.jpg");  //client.send(scanner.next());
        } else {
            getAddress();
            server.getPort();
            server.receive();
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
