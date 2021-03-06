import java.util.Date;

/**
 * Created by Kristo on 5.11.2016.
 */
public class Request {

    private String id;
    private String downloadIp;
    private String fileIp;
    private Date stamp;

    public Request(String id, String downloadIp, String fileIp) {
        this.id = id;
        this.downloadIp = downloadIp;
        this.fileIp = fileIp;
        this.stamp = new Date(System.currentTimeMillis());
    }

    public String getId() {
        return id;
    }

    public String getDownloadIp() {
        return downloadIp;
    }

    public String getFileIp() {
        return fileIp;
    }

    public Date getStamp() {
        return stamp;
    }

    public void setFileIp(String fileIp) {
        this.fileIp = fileIp;
    }
}
