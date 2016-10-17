/**
 * Created by Kristo on 15.10.2016.
 */

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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

            LinkedHashMap<String, Neighbor> peers = new LinkedHashMap<>();
            NetworkWatcher nw = new NetworkWatcher(peers);
            nw.start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Stop() {
        server.stop(0);
        System.out.println("server stopped");
    }



}

