/**
 * Created by Kristo on 16.10.2016.
 */
public class Neighbor {

    private int index;
    private String ip;
    private int port;
    private boolean isAlive;

    public Neighbor(String ip, int port, boolean isAlive) {
        this.ip = ip;
        this.port = port;
        this.isAlive = isAlive;
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

    @Override
    public String toString() {
        return "Neighbor{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", isAlive=" + isAlive +
                '}';
    }
}
