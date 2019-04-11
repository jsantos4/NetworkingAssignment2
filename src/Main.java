import java.io.*;
import java.net.SocketException;
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
            System.out.println("Enter destination address ");
            String dest = "localhost"; //scanner.next();
            System.out.println("Enter port");
            int udpPort = scanner.nextInt();
            Client client = new Client(dest, udpPort);
            System.out.println("Enter path to file you wish to send");
            //client.send(scanner.next());
            client.send("/home/jsantos4/Documents/csc445/NetworkingAssignment2/resources/Interior2.jpg");
        } else {
            getAddress();
            server.getPort();
            System.out.println("Enter path to store file including file name (does not have to match sent file's name)");
            try {
                server.receive(scanner.next());
            } catch (SocketException e) {
                e.printStackTrace();
            }
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
