package petrglad.msgsort;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;

public class AhcHttpClient {

    // To Isolate users of this class from AHC library (can be moved to top level).
    public static class TextResult {
        public final int statusCode;
        public final String body;

        public TextResult(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }

    public TextResult get(URI uri) {
        try {
            /* To speed this up we can
               1. Keep client open (re-use it) for several requests
               2. Ensure connection to server has 'keep-alive' header */
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                try (CloseableHttpResponse response = httpclient.execute(new HttpGet(uri))) {
                    return new TextResult(
                            response.getStatusLine().getStatusCode(),
                            EntityUtils.toString(response.getEntity()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
