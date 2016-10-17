import java.util.Date;

/**
 * Created by Kristo on 17.10.2016.
 */
public class Request {

    private String id;
    private String url;
    private Date stamp;

    public Request(String id, String url) {
        this.id = id;
        this.url = url;
        this.stamp = new Date(System.currentTimeMillis());
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Date getStamp() {
        return stamp;
    }
}
