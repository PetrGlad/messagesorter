package petrglad.msgsort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message processing utils
 */
public class Message {
    final LocalDateTime timestamp;
    final long value;

    public Message(String formattedMessage) {
        String[] pieces = formattedMessage.split(";");
        if (pieces.length != 2) {
            throw new IllegalArgumentException(
                    "Expected 2 fields got " + pieces.length + ", messageStr=" + formattedMessage);
        }
        timestamp = LocalDateTime.parse(pieces[0], DateTimeFormatter.ISO_DATE_TIME);
        value = Long.parseLong(pieces[1].trim());
    }

    @Override
    public String toString() {
        return "Message{" +
                "timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }
}
