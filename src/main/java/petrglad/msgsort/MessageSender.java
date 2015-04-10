package petrglad.msgsort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import java.util.function.Function;

public class MessageSender {
    private static final Logger LOG = LoggerFactory.getLogger(MessageSender.class);
    public static final String VALUE_PLACE = "{value}";
    public static final String TIMESTAMP_PLACE = "{timestamp}";

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

    static Function<Message, URI> getMessageURIFunction(String destUriFormat) {
        String formatStr = destUriFormat.replace("%", "%%")
                .replace(TIMESTAMP_PLACE, "%1s")
                .replace(VALUE_PLACE, "%2s");
        return m -> {
            try {
                return new URI(String.format(formatStr,
                        Messages.formatTimestamp(m.timestamp),
                        Long.toString(m.value)));
            } catch (URISyntaxException e) {
                throw new RuntimeException(
                        "Destination URI '" + destUriFormat + "' is incorrect for message '" + m + "'",
                        e);
            }
        };
    }
}
