/**
 * Created by Kristo on 16.10.2016.
 */
public class Neighbor {

    private int index;
    private String ip;
    private int port;
    private boolean isAlive;

    public Neighbor(int index, String ip, int port, boolean isAlive) {
        this.index = index;
        this.ip = ip;
        this.port = port;
        this.isAlive = isAlive;
    }

    public int getIndex() {
        return index;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }
}
