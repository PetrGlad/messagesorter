package petrglad.msgsort;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

public class AhcHttpClient implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(AhcHttpClient.class);

    private final CloseableHttpClient httpclient = HttpClients.createDefault();

    // To Isolate users of this class from AHC library (can be moved to top level).
    public static class TextResult {
        public final int statusCode;
        public final String body;

        public TextResult(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public boolean isSuccessful() {
            return statusCode == HttpStatus.OK_200;
        }

        @Override
        public String toString() {
            return "TextResult{" +
                    "statusCode=" + statusCode +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    public TextResult get(URI uri) {
        try {
            LOG.debug("HTTP GET {}", uri);

            // To speed this up we can ensure connection to server has 'keep-alive' header
            try (CloseableHttpResponse response = httpclient.execute(new HttpGet(uri))) {
                return new TextResult(
                        response.getStatusLine().getStatusCode(),
                        EntityUtils.toString(response.getEntity()));
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void close() throws IOException {
        httpclient.close();
    }
}
