import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kristo on 31.10.2016.
 */
public class FileRequestHandler extends SimpleHttpServer implements HttpHandler{

    @Override
    public void handle(HttpExchange he) throws IOException {
        System.out.println("Received a /file request");

        Map<String, Object> parameters = new HashMap<String, Object>();
        URI requestedUri = he.getRequestURI();
        String query = requestedUri.getRawQuery();
        parseQuery(query, parameters);
        // send response

        String response;

        if (parameters.get("id") != null) {
            response = "OK";
            he.sendResponseHeaders(200, response.length());
        } else {
            response = "ERROR - missing parameter 'id'";
            he.sendResponseHeaders(422, response.length());
        }

        OutputStream os = he.getResponseBody();
        os.write(response.getBytes());
        os.close();

        String idParam = parameters.get("id").toString();
        String from = he.getRequestHeaders().getFirst("Host");

        if (idParam != null) {

            if (!routingTableContainsRequest(idParam)) {
                addFileRequestToRoutingTable(idParam, from);
                return;
            }

            if (getMyRequests().containsKey(idParam)) {
                System.out.println("Received a response for my request with an id:'" + idParam + "' from: '" + from + "'");
                InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);



            }

        }

        Map<String, Object> parameters2 = new HashMap<>();
        InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String query2 = br.readLine();
        parseQuery(query2, parameters2);
        // send response
        String response2 = "";
        for (String key : parameters2.keySet()) {
            response += key + " = " + parameters2.get(key) + "\n";
        }
        he.sendResponseHeaders(200, response2.length());
        OutputStream os2 = he.getResponseBody();
        os2.write(response2.toString().getBytes());
        os2.close();

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
