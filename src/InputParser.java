import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
                Set set = server.getPeers().entrySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry me = (Map.Entry) iterator.next();
                    if (!((Neighbor) me.getValue()).isAlive()) {
                        continue;
                    }
                    String address = "http://google.com";
                    URL url = null;
                    try {
                        url = new URL("http://" + ((Neighbor) me.getValue()).getIp() + ":" + ((Neighbor) me.getValue()).getPort() + "/download?" + "id=" + generateMyNumber() + "&url=" + address);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Constructed url: " + url);
                    HttpURLConnection conn = null;
                    try {
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        if (conn.getResponseCode() == 200) {
                            System.out.println("Connection opened " + conn.getResponseCode());

                            System.out.println("Sending request to URL : " + url);
                            System.out.println("Response Code : " + conn.getResponseCode());
                            System.out.println("Response Message : " + conn.getResponseMessage());

                            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            System.out.println("Connection opened");
                            System.out.println("Forwarded message to " + ((Neighbor) me.getValue()).getIp() + ":" + ((Neighbor) me.getValue()).getPort());

                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(conn.getInputStream()));
                            String line;
                            StringBuffer response2 = new StringBuffer();

                            while ((line = in.readLine()) != null) {
                                response2.append(line);
                            }
                            in.close();

                            System.out.println(response2.toString());

                        } else {
                            System.out.println("No answer form: " + ((Neighbor) me.getValue()).getIp());
                            continue;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (splitted[0].equals("laziness") && splitted[1] != null) {
                server.setLaziness(Double.parseDouble(splitted[1]));
                System.out.println("Laziness set to " + server.getLaziness());
            }

        }
    }

}

