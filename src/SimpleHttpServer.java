/**
 * Created by Kristo on 15.10.2016.
 */

import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;

public class SimpleHttpServer {
    private int port;
    private HttpServer server;
    private ArrayList<Neighbor> neighbours = new ArrayList<>();

    public void Start(int port) {
        try {
            this.port = port;
            server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("server started at " + port);
            server.createContext("/", new Handlers.RootHandler());
            server.createContext("/download", new Handlers.DownloadHandler());
            server.createContext("/header", new Handlers.HeaderHandler());
            server.createContext("/get", new Handlers.GetHandler());
            server.createContext("/post", new Handlers.PostHandler());
            server.setExecutor(null);
            server.start();

            Worker neighborWatcher  = new Worker(neighbours);
            neighborWatcher.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Stop() {
        server.stop(0);
        System.out.println("server stopped");
    }

}

class Worker extends Thread {

    ArrayList<Neighbor> neighbors;
    //String url = "http://192.168.3.11:1215/getpeers";

    Worker(ArrayList<Neighbor> neighbors) throws IOException {
        this.neighbors = neighbors;
    }

    public void run() {
        try {
            while (true) {
                StringBuilder result = new StringBuilder();
                URL url = new URL("http://192.168.3.11:1215/getpeers");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                if (!result.equals("[]")) {
                    String res = "[\"192.168.3.38:1215\", \"192.168.3.26:1215\"]";
                    String[] arr = result.toString().split(",");
                    ArrayList<String> list = new ArrayList<>();
                    for (String s : arr) {
                        list.add(s.split("\"")[1]);
                    }
                    for (int i = 0; i < list.size(); i++) {
                        Neighbor n = new Neighbor(i, list.get(i).split(":")[0], Integer.parseInt(list.get(i).split(":")[1]), true);
                        System.out.println(n);
                    }
                }

                Thread.sleep(60 * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
