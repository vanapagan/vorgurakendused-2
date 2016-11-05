import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Created by Kristo on 31.10.2016.
 */
public class DownloadRequestHandler extends SimpleHttpServer implements HttpHandler{

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

        String idParam = parameters.get("id").toString();
        String urlParam = parameters.get("url").toString();

        if (idParam != null && urlParam != null) {

            if (routingTableContainsRequest(idParam, urlParam)) {
                return;
            }

            double d = new Random().nextDouble();
            System.out.println(d);
            if (d < getLaziness()) {
                //TODO download file and construct /file post message
                System.out.println("---DOWNLOAD---");
                StringBuilder result = new StringBuilder();
                URL url = new URL(urlParam);
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

                String body = sb.toString();
                System.out.println("ResponseBody constructed");

                StringBuilder tokenUri = new StringBuilder("id=");
                tokenUri.append(URLEncoder.encode(body,"UTF-8"));

                String url2 = "https://" + he.getRequestHeaders().getFirst("Host") + "/file?id=" + idParam;
                System.out.println(url2);
                URL obj = new URL(url2);
                HttpsURLConnection con = null;
                try {
                    con = (HttpsURLConnection) obj.openConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                con.setRequestMethod("POST");
                con.setRequestProperty("Accept-Language", "UTF-8");

                con.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
                outputStreamWriter.write(tokenUri.toString());
                outputStreamWriter.flush();

                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + url2);
                System.out.println("Post parameters : " + sb.toString());
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response2 = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response2.append(inputLine);
                }
                in.close();

                System.out.println(response2.toString());

            } else {
                //TODO do not download, but send request to everyone else in the network
                System.out.println("---FORWARD---");
                Set set = getPeers().entrySet();

                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry me = (Map.Entry) iterator.next();
                    if (!((Neighbor) me.getValue()).isAlive()) {
                        continue;
                    }
                    URL url = new URL("http://" + ((Neighbor) me.getValue()).getIp() + ":" + ((Neighbor) me.getValue()).getPort() + "/download?" + "id=" + idParam + "&" + "url=" + urlParam);
                    System.out.println("Constructed url: " + url);

                    new DownloadThread(super.getRoutingTable(), url).start();

                }
            }

                /*
                if (he.getRequestMethod().equals("GET")) {
                    new GetHandler().handle(he);
                } else {
                    new PostHandler().handle(he);
                }*/

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
