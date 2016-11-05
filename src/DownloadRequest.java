import java.util.Date;

/**
 * Created by Kristo on 5.11.2016.
 */
public class DownloadRequest {

    private String id;
    private String downloadIp;
    private String fileIp;
    private Date stamp;

    public DownloadRequest(String id, String downloadIp) {
        this.id = id;
        this.downloadIp = downloadIp;
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
