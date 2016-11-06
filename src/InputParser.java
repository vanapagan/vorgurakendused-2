import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Kristo on 20.10.2016.
 */
public class InputParser extends Thread {

    private Scanner scanner;
    private SimpleHttpServer server;

    public InputParser(Scanner scanner, SimpleHttpServer server) {
        this.scanner = scanner;
        this.server = server;
    }

    public static String generateMyNumber() {
        int aNumber = 0;
        aNumber = (int) ((Math.random() * 9000000) + 1000000);
        return Integer.toString(aNumber);
    }

    public void run() {
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            System.out.println("You entered: " + input);

            String[] splitted = input.split(" ");

            if (splitted[0].equals("download")) {
                System.out.println("---SELF DOWNLOAD---");

                long threadId = Thread.currentThread().getId();
                System.out.println("Thread # " + threadId + " is doing this task (InputParser)");

                String idParam = generateMyNumber();
                //String address = "http://google.com";
                String address = splitted[1];

                //MyRequest myr = new MyRequest(idParam, address);
                //server.getMyRequests().put(idParam, myr);

                if (server.routingTableContainsRequest(idParam)) {
                    System.out.println("Routing table already contains a request id '" + idParam + "'");
                    continue;
                } else {
                    server.addDownloadRequestToRoutingTable(idParam, "http://localhost:1215");
                    System.out.println("Size of the routingTable: " + server.getRoutingTable().size());
                }

                if (server.getPeers().entrySet() == null) {
                    System.out.println("Sorry no neighbours in the network");
                    continue;
                }

                Set set = server.getPeers().entrySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry me = (Map.Entry) iterator.next();

                    if (!((Neighbor) me.getValue()).isAlive()) {
                        continue;
                    }

                    URL url = null;

                    try {
                        url = new URL("http://" + ((Neighbor) me.getValue()).getIp() + ":" + ((Neighbor) me.getValue()).getPort() + "/download?" + "id=" + idParam + "&url=" + address);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Constructed url: " + url);
                    new DownloadThread(url).start();
                }
            } else if (splitted[0].equals("laziness") && splitted[1] != null) {
                server.setLaziness(Double.parseDouble(splitted[1]));
                System.out.println("Laziness set to " + server.getLaziness());
            } else if (splitted[0].equals("size")) {
                System.out.println("Size of the routing table: " + server.getRoutingTable().size());
            }

        }
    }

}

