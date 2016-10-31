import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * Created by Kristo on 31.10.2016.
 */
public class FileResponseHandler extends SimpleHttpServer implements HttpHandler{

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Received a file response");
    }

}
