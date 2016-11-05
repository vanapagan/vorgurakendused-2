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

    private static LinkedHashMap<String, MyRequest> in = new LinkedHashMap<String, MyRequest>();
    private static LinkedHashMap<String, Neighbor> peers = new LinkedHashMap<String, Neighbor>();
    private LinkedHashMap<String, DownloadRequest> routingTable = new LinkedHashMap<String, DownloadRequest>();
    private LinkedHashMap<String, MyRequest> myRequests = new LinkedHashMap<String, MyRequest>();

    private double laziness = 0.9999;

    public void Start(int port) {
        try {
            this.port = port;
            server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("server started at " + port);
            server.createContext("/", new Handlers.RootHandler());

            DownloadRequestHandler downloadHandler = new DownloadRequestHandler();
            FileRequestHandler fileHandler = new FileRequestHandler();

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

    public boolean routingTableContainsRequest(String id, String url) {
        if (getRoutingTable().containsKey(id)) {
            System.out.println("/download request with an id: " + id + " has already been served and will be ignored");
            return true;
        } else {
            addRequestToRoutingTable(id, url);
            return false;
        }
    }

    private void addRequestToRoutingTable(String id, String url) {
        DownloadRequest dlr = new DownloadRequest(id, url);
        getRoutingTable().put(id, dlr);
        System.out.println("/download request with an id: " + id + " has been added to the  routing table");
    }

    public LinkedHashMap<String, MyRequest> getMyRequests() {
        return myRequests;
    }
}

