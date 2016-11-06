import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Kristo on 17.10.2016.
 */
public class NetworkWatcher extends Thread {

    private LinkedHashMap<String, Neighbor> peers = new LinkedHashMap<String, Neighbor>();
    private URL url = new URL("http://192.168.3.11:1215/getpeers");
    //private URL url = new URL("http://www.tud.ttu.ee/im/Tarvo.Treier/");

    public NetworkWatcher(LinkedHashMap<String, Neighbor> peers) throws IOException {
        this.peers = peers;
    }

    public synchronized void run() {
        try {
            while (true) {
                //System.out.println("Updated network isAlive HashTable " + new SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.UK).format(new Date()));

                StringBuilder result = new StringBuilder();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;

                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                if (!result.equals("[]")) {
                    //String res = "[\"192.168.3.38:1215\", \"192.168.3.26:1215\"]";
                    String[] arr = result.toString().split(",");
                    //String[] arr = res.toString().split(",");

                    if (peers.size() > 0) {
                        Set set = peers.entrySet();

                        Iterator iterator = set.iterator();
                        while(iterator.hasNext()) {
                            Map.Entry me = (Map.Entry)iterator.next();
                            ((Neighbor) me.getValue()).setAlive(false);
                        }
                    }

                    for (String s : arr) {
                        String arrayItem = s.split("\"")[1];
                        String ip = arrayItem.split(":")[0];
                        int port = Integer.parseInt(arrayItem.split(":")[1]);
                        Neighbor n = new Neighbor(ip, port, true);

                        if (!peers.containsKey(ip)) {
                            peers.put(ip, n);
                            //System.out.println("Added a new peer to the watchlist: " + n.getIp());
                        } else if (peers.containsKey(ip)) {
                            peers.get(ip).setAlive(true);
                        }

                        //System.out.println(n.toString());

                    }
                }
                Thread.sleep(60 * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LinkedHashMap<String, Neighbor> getPeers() {
        return peers;
    }

}
