/**
 * Created by Kristo on 15.10.2016.
 */

public class Main {
    public static int port = 1215;

    public static void main(String[] args) {

		SimpleHttpServer httpServer = new SimpleHttpServer();
		httpServer.Start(port);

    }
}


