/**
 * Created by Kristo on 15.10.2016.
 */

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class Handlers extends SimpleHttpServer {
    @SuppressWarnings("unchecked")
    public static void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

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

    public static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange he) throws IOException {
            String response = "<h1>Server start success if you see this message</h1>" + "<h1>Port: " + Main.port + "</h1>";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static class DownloadHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {

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

            System.out.println("Received a /download request");

            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();


            if (parameters.get("id") != null && parameters.get("url") != null) {
                Request request = new Request(parameters.get("id").toString(), parameters.get("url").toString());
                getIn().put(parameters.get("id").toString(), request);

                double d = new Random().nextDouble();
                if (d >= 0.0001) {
                    //TODO download file and construct /file post message
                    System.out.println("I will download");
                    StringBuilder result = new StringBuilder();
                    URL url = new URL(parameters.get("url").toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    int status = conn.getResponseCode();
                    String mime = conn.getContentType();

                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    rd.close();
                    String content = result.toString();

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
                        sb.append("\"" + content + "\"");
                        sb.append("}");
                    }

                    String responseBody = sb.toString();
                    //System.out.println(responseBody);

                } else {
                    //TODO do not download, but send request to everyone else in the network
                    Set set = getPeers().entrySet();

                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry me = (Map.Entry) iterator.next();
                        if (((Neighbor) me.getValue()).getIp().equals("192.168.3.39") && iterator.hasNext()) {
                            me = (Map.Entry) iterator.next();
                        }
                        if (!((Neighbor) me.getValue()).isAlive()) {
                            continue;
                        }

                        URL url = new URL("http://" + ((Neighbor) me.getValue()).getIp() + ":" + ((Neighbor) me.getValue()).getPort() + "/download?" + "id=" + parameters.get("id").toString() + "&" + "url=" + parameters.get("url").toString());
                        System.out.println("Constructed url: " + url);
                        HttpURLConnection conn = null;
                        try {
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setConnectTimeout(5000);
                            conn.setReadTimeout(5000);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (conn.getResponseCode() == 200) {
                            System.out.println("Connection opened " + conn.getResponseCode());

                            System.out.println("Sending request to URL : " + url);
                            System.out.println("Response Code : " + conn.getResponseCode());
                            System.out.println("Response Message : " + conn.getResponseMessage());

                            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            System.out.println("Connection opened");
                            System.out.println("Forwarded message to " + ((Neighbor) me.getValue()).getIp() + ":" + ((Neighbor) me.getValue()).getPort());

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
                            System.out.println("No answer form: " + ((Neighbor) me.getValue()).getIp());
                            continue;
                        }
                    }

                }

                if (he.getRequestMethod().equals("GET")) {
                    new GetHandler().handle(he);
                } else {
                    new PostHandler().handle(he);
                }

            }


        }
    }

    public static class HeaderHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            Headers headers = he.getRequestHeaders();
            Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
            String response = "";
            for (Map.Entry<String, List<String>> entry : entries)
                response += entry.toString() + "\n";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }
    }

    public static class GetHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            System.out.println("Served by /get handler...");
            // parse request
            Map<String, Object> parameters = new HashMap<>();
            URI requestedUri = he.getRequestURI();
            String query = requestedUri.getRawQuery();
            parseQuery(query, parameters);
            // send response
            String response = "";
            for (String key : parameters.keySet())
                response += key + " = " + parameters.get(key) + "\n";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }

    }

    public static class PostHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            System.out.println("Served by /post handler...");
            // parse request
            Map<String, Object> parameters = new HashMap<>();
            InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String query = br.readLine();
            parseQuery(query, parameters);
            // send response
            String response = "";
            for (String key : parameters.keySet())
                response += key + " = " + parameters.get(key) + "\n";
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();

        }
    }
}

