import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by Kristo on 31.10.2016.
 */
public class DownloadRequestHandler implements HttpHandler {

    private LinkedHashMap<String, Request> routingTable;
    private LinkedHashMap<String, Neighbor> peers;
    private double laziness;

    public DownloadRequestHandler (LinkedHashMap<String, Request> routingTable, LinkedHashMap<String, Neighbor> peers, double laziness) {
        this.routingTable = routingTable;
        this.peers = peers;
        this.laziness = laziness;
    }

    @Override
    public void handle(HttpExchange he) throws IOException {

        System.out.println("Received a /download request");

        long threadId = Thread.currentThread().getId();
        System.out.println("Thread # " + threadId + " is doing this task (DownloadRequestHandler)");

        Map<String, Object> parameters = new HashMap<String, Object>();
        URI requestedUri = he.getRequestURI();
        String query = requestedUri.getRawQuery();
        parseQuery(query, parameters);
        // send response

        String response;

        if (parameters.get("id") != null && parameters.get("url") != null) {
            response = "OK";
            he.sendResponseHeaders(200, response.length());
        } else {
            response = "ERROR - missing either 'id' or 'url' parameter or both...";
            he.sendResponseHeaders(422, response.length());
        }

        //get host sender ip
        String from = he.getRequestHeaders().getFirst("Host");

        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();

        String idParam = parameters.get("id").toString();
        String urlParam = parameters.get("url").toString();

        if (idParam != null && urlParam != null) {

            if (routingTable.containsKey(idParam)) {
                System.out.println("Routing table already contains request id: " + idParam);
                return;
            } else {
                Request dlr = new Request(idParam, from, null);
                routingTable.put(idParam, dlr);
                System.out.println("Size of the routingTable: " + routingTable.size());
            }

            double d = new Random().nextDouble();
            System.out.println(d);

            if (d < laziness) {
                //TODO download file and construct /file post message
                System.out.println("---DOWNLOAD---");
                StringBuilder result = new StringBuilder();
                URL url = new URL(urlParam);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                int status = conn.getResponseCode();
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

                byte[] byteArr = result.toString().getBytes("UTF-8");
                String encodedContent = Base64.getEncoder().encodeToString(byteArr);

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
                    sb.append("\"" + encodedContent + "\"");
                    sb.append("}");
                }

                String body = sb.toString();
                System.out.println("ResponseBody constructed");

                String url2 = "http://" + from + "/file?id=" + idParam;
                URL urlObj = new URL(url2);
                FileThread ft = new FileThread(urlObj, body);
                ft.start();

            } else {
                //TODO do not download, but send request to everyone else in the network
                System.out.println("---FORWARD---");
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
                    URL url = new URL("http://" + ((Neighbor) me.getValue()).getIp() + ":" + ((Neighbor) me.getValue()).getPort() + "/download?" + "id=" + idParam + "&" + "url=" + urlParam);
                    System.out.println("Constructed url: " + url);

                    new DownloadThread(url).start();
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

}


