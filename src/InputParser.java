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
                System.out.println("---FORWARD---");

                String idParam = generateMyNumber();
                String address = "http://google.com";
                //String address = splitted[1];

                MyRequest myr = new MyRequest(idParam, address);
                server.getMyRequests().put(idParam, myr);
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
                    new DownloadThread(server.getRoutingTable(), url).start();
                }
            } else if (splitted[0].equals("laziness") && splitted[1] != null) {
                server.setLaziness(Double.parseDouble(splitted[1]));
                System.out.println("Laziness set to " + server.getLaziness());
            }

        }
    }

}

