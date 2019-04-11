import java.io.*;
import java.net.SocketException;
import java.net.URL;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        int side;
        System.out.println("Client = 0, Server = 1");
        Scanner scanner = new Scanner(System.in);
        side = Integer.parseInt(scanner.nextLine());
        Server server = new Server();

        if (side == 0) {
            System.out.println("Enter destination address ");
            String dest = scanner.nextLine();

            System.out.println("Enter port");
            int udpPort = Integer.parseInt(scanner.nextLine());

            System.out.println("Enter path to file you wish to send");
            String path = scanner.nextLine();

            System.out.println("Select options for send: IPv4/IPv6, Sequential/SlidingWindows, no drops/1% drops (-4/6 -s/w -n/d)");
            String options = scanner.nextLine();
            boolean[] selection = parseOptions(options);

            Client client = new Client(dest, udpPort, selection);
            client.send(path);

        } else {
            getAddress();
            server.getPort();
            System.out.println("Enter path to store file including file name (does not have to match sent file's name)");
            try {
                server.receive(scanner.nextLine());
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean[] parseOptions(String options) {
        boolean[] selection = new boolean[3];

        if (options.contains("6"))
            selection[0] = true;
        if (options.contains("w"))
            selection[1] = true;
        if (options.contains("d"))
            selection[2] = true;

        //Options are not ordered. If any of these characters appear in options input, the tftp will act accordingly
        //Otherwise default behavior will be, IPv4, sequential, and no drops

        return selection;
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
