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
    private LinkedHashMap<String, Neighbor> peers = new LinkedHashMap<String, Neighbor>();
    private LinkedHashMap<String, Request> routingTable = new LinkedHashMap<String, Request>();
    private LinkedHashMap<String, MyRequest> myRequests = new LinkedHashMap<String, MyRequest>();

    private double laziness = 0.9999;

    public void Start(int port) {
        try {

            long threadId = Thread.currentThread().getId();
            System.out.println("Thread # " + threadId + " is doing this task (DownloadRequestHandler)");

            this.port = port;
            server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("server started at " + port);
            server.createContext("/", new Handlers.RootHandler());

            server.createContext("/download", new DownloadRequestHandler(routingTable, peers, laziness));
            server.createContext("/file", new FileRequestHandler(routingTable, peers));
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

    public LinkedHashMap<String, Neighbor> getPeers() {
        return peers;
    }

    public double getLaziness() {
        return laziness;
    }

    public void setLaziness(double laziness) {
        this.laziness = laziness;
    }

    public LinkedHashMap<String, Request> getRoutingTable() {
        return routingTable;
    }

    public boolean routingTableContainsRequest(String id) {
        if (routingTable.containsKey(id)) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized void addDownloadRequestToRoutingTable(String id, String url) {
        Request dlr = new Request(id, url, null);
        routingTable.put(id, dlr);
        System.out.println("/download request with an id: " + id + " has been added to the  routing table");
    }

    public void addFileRequestToRoutingTable(String id, String url) {
        Request dlr = new Request(id, null, url);
        routingTable.put(id, dlr);
        System.out.println("/file request with an id: " + id + " has been added to the  routing table");
    }

    public LinkedHashMap<String, MyRequest> getMyRequests() {
        return myRequests;
    }
}

