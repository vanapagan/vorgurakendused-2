import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Kristo on 5.11.2016.
 */
public class FileThread extends Thread {

    URL url;
    StringBuilder tokenUri;
    String body;


    public FileThread(URL url, StringBuilder tokenUri, String body) {
        this.url = url;
        this.tokenUri = tokenUri;
        this.body = body;
    }

    public void run() {
        try {
            StringBuilder tokenUri = new StringBuilder("id=");
            tokenUri.append(URLEncoder.encode(body,"UTF-8"));

            URL obj = url;
            HttpsURLConnection con = null;

            try {
                con = (HttpsURLConnection) obj.openConnection();
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
            outputStreamWriter.write(tokenUri.toString());
            outputStreamWriter.flush();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + body);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response2 = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response2.append(inputLine);
            }
            in.close();

            System.out.println(response2.toString());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
