import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by Kristo on 31.10.2016.
 */
public class FileRequestHandler extends SimpleHttpServer implements HttpHandler {

    private LinkedHashMap<String, Request> routingTable;
    private LinkedHashMap<String, Neighbor> peers;

    public FileRequestHandler (LinkedHashMap<String, Request> routingTable, LinkedHashMap<String, Neighbor> peers) {
        this.routingTable = routingTable;
        this.peers = peers;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {
        System.out.println("Received a /file request");

        // fetch body's content for later use, before the stream is closed
        String body = getRequestBody(he.getRequestBody());

        long threadId = Thread.currentThread().getId();
        System.out.println("Thread # " + threadId + " is doing this task (FileRequestHandler)");

        Map<String, Object> parameters = new HashMap<String, Object>();
        URI requestedUri = he.getRequestURI();
        String query = requestedUri.getRawQuery();
        parseQuery(query, parameters);
        // send response

        String response;

        String idParam = parameters.get("id").toString();
        String from = he.getRequestHeaders().getFirst("Host");

        if (idParam != null) {
            response = "OK";
            he.sendResponseHeaders(200, response.length());
        } else {
            response = "ERROR - missing parameter 'id'";
            he.sendResponseHeaders(422, response.length());
        }

        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();

        if (idParam != null) {

            if (!routingTable.containsKey(idParam)) {
                System.out.println("Routing table does not contain request " + idParam);
                Request dlr = new Request(idParam, null, from);
                routingTable.put(idParam, dlr);
                System.out.println("Size of the routingTable: " + routingTable.size());

                System.out.println("Going to return");
                return;
            } else {
                System.out.println("Routing table contains request " + idParam);
                System.out.println("Size of the routingTable: " + routingTable.size());
            }

            if (routingTable.containsKey(idParam) && routingTable.get(idParam).getDownloadIp() == null) {
                System.out.println("/file request " + idParam + " has not been requested through this server and will be ignored!");
                return;
            }

            if (routingTable.containsKey(idParam) && routingTable.get(idParam).getDownloadIp() == null && routingTable.get(idParam).getFileIp() == null) {
                System.out.println("/file request " + idParam + " has already been served through this server and will be ignored!");
                return;
            }

            if (routingTable.containsKey(idParam) && routingTable.get(idParam).getDownloadIp().equals("localhost:1215")) {
                routingTable.get(idParam).setFileIp(from);
                System.out.println("Received a /file response for my request id:'" + idParam + "' from: '" + from + "'");
                System.out.println("RESPONSE for my request '" + idParam + "' : " + body);
            } else if (routingTable.containsKey(idParam) && !routingTable.get(idParam).getDownloadIp().equals("localhost:1215") && routingTable.get(idParam).getDownloadIp() != null) {
                routingTable.get(idParam).setFileIp(from);
                URL url = new URL("http://" + routingTable.get(idParam).getDownloadIp() + "/file?" + "id=" + idParam);
                System.out.println("Constructed url: " + url);
                FileThread ft = new FileThread(url, body);
                ft.start();
            } else {
                System.out.println("Forward /file message to all neighbours, except the requester itself");
                Set set = peers.entrySet();

                Iterator iterator = set.iterator();

                while (iterator.hasNext()) {
                    Map.Entry me = (Map.Entry) iterator.next();
                    if (!((Neighbor) me.getValue()).isAlive()) {
                        continue;
                    }
                    // do not sent to the requester itself
                    if (((Neighbor) me.getValue()).getIp().equals(from)) {
                        continue;
                    }
                    URL url = new URL("http://" + ((Neighbor) me.getValue()).getIp() + ":" + ((Neighbor) me.getValue()).getPort() + "/file?" + "id=" + idParam);

                    System.out.println("Constructed url: " + url);

                    FileThread ft = new FileThread(url, body);
                    ft.start();
                }
            }

        }

    }

    public void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

        if (query != null) {
            String pairs[] = query.split("[&]");

            for (String pair : pairs) {
                String param[] = pair.split("[=]");

                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);
                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }

    private String getRequestBody(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return  sb.toString();
    }

}
