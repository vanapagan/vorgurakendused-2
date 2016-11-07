import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

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
        int aNumber;
        aNumber = (int) ((Math.random() * 9000000) + 1000000);
        return Integer.toString(aNumber);
    }

    public void run() {
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            System.out.println("---CLI API---");
            System.out.println("You entered: " + input);

            String[] splitted = input.split(" ");

            if (splitted[0].equals("download")) {

                long threadId = Thread.currentThread().getId();
                System.out.println("Thread # " + threadId + " is doing this task (InputParser)");

                String idParam = generateMyNumber();
                String address;

                if (splitted.length == 1) {
                    address = "http://www.tud.ttu.ee/im/Tarvo.Treier/";
                } else {
                    address = splitted[1].trim();
                }

                if (server.routingTableContainsRequest(idParam)) {
                    System.out.println("Routing table already contains a request id '" + idParam + "'");
                    continue;
                } else {
                    server.addDownloadRequestToRoutingTable(idParam, "http://localhost:1215");
                    System.out.println("Size of the routingTable: " + server.getRoutingTable().size());
                }

                double d = new Random().nextDouble();
                System.out.println(d);

                if (d < server.getLaziness()) {
                    //TODO download file and print it here
                    try {
                        System.out.println("---DOWNLOAD---");
                        StringBuilder result = new StringBuilder();
                        URL url = null;
                        url = new URL(address);

                        HttpURLConnection conn;
                        conn = (HttpURLConnection) url.openConnection();

                        conn.setRequestMethod("GET");

                        BufferedReader rd;
                        rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        String line;
                        int status = 0;
                        status = conn.getResponseCode();

                        String[] contentTypeArray = conn.getContentType().split(";");
                        String mime;

                        if (contentTypeArray[0] != null) {
                            mime = contentTypeArray[0];
                        } else {
                            mime = "unknown";
                        }
                        while ((line = rd.readLine()) != null) {
                            result.append(line);
                        }

                        rd.close();

                        String content = result.toString();

                        StringBuilder sb = new StringBuilder();
                        if (conn.getResponseCode() != 200) {
                            System.out.println("Http error " + conn.getResponseCode());
                            sb.append("{\"status\":");
                            sb.append(conn.getResponseCode());
                            sb.append("}");
                        } else {
                            sb.append("{\"status\":");
                            sb.append(status);
                            sb.append(", \"mime-type\":\"");
                            sb.append(mime + "\", ");
                            sb.append("\"content\":");
                            sb.append("\"" + content + "\"");
                            sb.append("}");
                        }

                        String body = sb.toString();
                        System.out.println("ResponseBody constructed");

                        System.out.println("RESPONSE to my request id '" + idParam + "' : " + body);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //TODO do not download, but send request to everyone else in the network
                    System.out.println("---FORWARD---");
                    Set set = server.getPeers().entrySet();

                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry me = (Map.Entry) iterator.next();
                        if (!((Neighbor) me.getValue()).isAlive()) {
                            continue;
                        }
                        URL url = null;
                        try {
                            url = new URL("http://" + ((Neighbor) me.getValue()).getIp() + ":" + ((Neighbor) me.getValue()).getPort() + "/download?" + "id=" + idParam + "&" + "url=" + address);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Constructed url: " + url);

                        new DownloadThread(url).start();
                    }
                }
                System.out.println("Finished dealing with the  CLI operation '" + input + "'");
            } else if (splitted[0].equals("laziness") && splitted[1] != null) {
                server.setLaziness(Double.parseDouble(splitted[1]));
                System.out.println("Laziness set to " + server.getLaziness());
            } else if (splitted[0].equals("size")) {
                System.out.println("Size of the routing table: " + server.getRoutingTable().size());
            } else {
                continue;
            }

        }
    }

}

