package petrglad.msgsort.stub;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static spark.Spark.get;
import static spark.Spark.port;

/**
 * Testing helper: simulates destination server for outgoing messages.
 */
public class DestinationStub {
    public static void main(String[] args) {
        final AtomicReference<LocalDateTime> bound = new AtomicReference<>(LocalDateTime.MIN);
        port(9200);
        get("/incoming", (req, res) -> {
            System.out.println("MSG " + req.queryString());
            // (It would be more convenient to have map as result of parse)
            final Optional<NameValuePair> timestamp =
                    URLEncodedUtils.parse(req.queryString(), StandardCharsets.UTF_8).stream()
                            .filter(nvp -> nvp.getName().equals("timestamp"))
                            .findAny();
            // Update max timestamp, show error if messages are not ordered:
            timestamp.ifPresent(nameValuePair -> {
                LocalDateTime newTimestamp = LocalDateTime.parse(
                        nameValuePair.getValue(),
                        DateTimeFormatter.ISO_DATE_TIME);
                bound.getAndUpdate(prev -> {
                    if (newTimestamp.isBefore(prev))
                        System.err.println("ERROR Message is not ordered " + req.queryString());
                    return newTimestamp;
                });
            });

            return "OK";
        });
    }
}

