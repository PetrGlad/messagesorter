package petrglad.msgsort;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.function.Function;

public class MessageSender {
    private static final Logger LOG = LoggerFactory.getLogger(MessageSender.class);

    static Consumer<? super Message> getMessageSender(Function<Message, URI> makeUri) {
        return m -> {
            try (AhcHttpClient httpClient = new AhcHttpClient()) {
                final AhcHttpClient.TextResult textResult = httpClient.get(makeUri.apply(m));
                if (textResult.isSuccessful()) {
                    LOG.info("Sent message={}, result={}", m, textResult);
                } else {
                    LOG.error("Error sending message={}, result={}", m, textResult);
                }
            } catch (IOException e) {
                LOG.error("Error sending message", e);
            }
        };
    }

    static Function<Message, URI> getMessageURIFunction(String destUri) {
        return m -> {
            try {
                URIBuilder ub = new URIBuilder(destUri);
                ub.addParameter("timestamp", Messages.formatTimestamp(m.timestamp));
                ub.addParameter("value", Long.toString(m.value));
                return ub.build();
            } catch (URISyntaxException e) {
                throw new RuntimeException(
                        "Destination URI '" + destUri + "' is incorrect for message '" + m + "'",
                        e);
            }
        };
    }
}
