import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.LinkedHashMap;

/**
 * Created by Kristo on 5.11.2016.
 */
public class DownloadThread extends Thread {

    private SimpleHttpServer server;
    private URL url;
    private HttpURLConnection conn = null;
    private LinkedHashMap<String, DownloadRequest> routingTable;

    public DownloadThread(LinkedHashMap<String, DownloadRequest> routingTable, URL url) {
        this.routingTable = routingTable;
        this.url = url;
    }

    public synchronized void run() {
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
        } catch (SocketTimeoutException e) {
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
                System.out.println("Forwarded message to " + url);

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
                System.out.println("No answer form: " + url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
