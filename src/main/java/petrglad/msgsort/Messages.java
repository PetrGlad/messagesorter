package petrglad.msgsort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Messages {
    public static String formatMessage(LocalDateTime timestamp, long value) {
        return formatTimestamp(timestamp) + ";" + value + "\n";
    }

    public static String formatTimestamp(LocalDateTime timestamp) {
        return timestamp.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
