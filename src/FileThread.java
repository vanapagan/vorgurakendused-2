import java.io.*;
import java.net.*;

/**
 * Created by Kristo on 5.11.2016.
 */
public class FileThread extends Thread {

    URL url;
    String body;


    public FileThread(URL url, String body) {
        this.url = url;
        this.body = body;
    }

    public void run() {
        try {
            StringBuilder encodedBody = new StringBuilder();
            encodedBody.append(URLEncoder.encode(body, "UTF-8"));

            URL obj = url;
            HttpURLConnection con = null;

            try {
                con = (HttpURLConnection) obj.openConnection();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "UTF-8");

            con.setDoOutput(true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
            outputStreamWriter.write(encodedBody.toString());
            outputStreamWriter.flush();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + body);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println(response.toString());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
