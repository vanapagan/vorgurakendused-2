import java.util.LinkedHashMap;

/**
 * Created by Kristo on 6.11.2016.
 */
public class RequestList {

    private LinkedHashMap<String, Request> routingTable = new LinkedHashMap<String, Request>();

    public LinkedHashMap<String, Request> getRoutingTable() {
        return routingTable;
    }

    public synchronized boolean routingTableContainsRequest(String id) {
        if (routingTable.containsKey(id)) {
            System.out.println("Routing table contains /download request with an id: " + id);
            return true;
        } else {
            System.out.println("Routing table does not contain /download request with an id: " + id);
            return false;
        }
    }

    public synchronized void addDownloadRequestToRoutingTable(String id, String url) {
        Request dlr = new Request(id, url, null);
        routingTable.put(id, dlr);
        System.out.println("/download request with an id: " + id + " has been added to the  routing table");
    }

    public synchronized void addFileRequestToRoutingTable(String id, String url) {
        Request dlr = new Request(id, null, url);
        routingTable.put(id, dlr);
        System.out.println("/file request with an id: " + id + " has been added to the  routing table");
    }

}
