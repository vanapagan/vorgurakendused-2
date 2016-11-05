/**
 * Created by Kristo on 15.10.2016.
 */

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class SimpleHttpServer {
    private int port;
    private HttpServer server;
    private static LinkedHashMap<String, Request> out = new LinkedHashMap<String, Request>();
    private static LinkedHashMap<String, Request> in = new LinkedHashMap<String, Request>();
    private static LinkedHashMap<String, Neighbor> peers = new LinkedHashMap<String, Neighbor>();
    private LinkedHashMap<String, DownloadRequest> routingTable = new LinkedHashMap<String, DownloadRequest>();
    private double laziness = 0.001;

    public void Start(int port) {
        try {
            this.port = port;
            server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("server started at " + port);
            server.createContext("/", new Handlers.RootHandler());

            DownloadRequestHandler downloadHandler = new DownloadRequestHandler();
            FileResponseHandler fileHandler = new FileResponseHandler();

            server.createContext("/download", downloadHandler);
            server.createContext("/file", fileHandler);
            server.createContext("/header", new Handlers.HeaderHandler());
            server.createContext("/get", new Handlers.GetHandler());
            server.createContext("/post", new Handlers.PostHandler());
            server.setExecutor(null);
            server.start();

            NetworkWatcher nw = new NetworkWatcher(peers);
            nw.start();

            InputParser parser = new InputParser(new Scanner(System.in), this);
            parser.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Stop() {
        server.stop(0);
        System.out.println("server stopped");
    }

    public static LinkedHashMap<String, Request> getOut() {
        return out;
    }

    public static LinkedHashMap<String, Request> getIn() {
        return in;
    }

    public static LinkedHashMap<String, Neighbor> getPeers() {
        return peers;
    }

    public double getLaziness() {
        return laziness;
    }

    public void setLaziness(double laziness) {
        this.laziness = laziness;
    }

    public LinkedHashMap<String, DownloadRequest> getRoutingTable() {
        return routingTable;
    }



}

