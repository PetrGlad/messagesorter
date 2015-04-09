package petrglad.msgsort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class MonotonicRestriction implements Consumer<Message> {

    private static final Logger LOG = LoggerFactory.getLogger(MonotonicRestriction.class);
    private final AtomicReference<LocalDateTime> boundary = new AtomicReference<>(LocalDateTime.MIN);

    @Override
    public void accept(Message message) {
        boundary.getAndUpdate(t -> Util.max(t, message.timestamp));
    }

    /**
     * Invokes 'after' only if message's timestamp is not behind boundary (not in past).
     */
    @Override
    public Consumer<Message> andThen(Consumer<? super Message> after) {
        Objects.requireNonNull(after);
        return (Message m) -> {
            accept(m);
            final LocalDateTime bound = boundary.get();
            if (bound.compareTo(m.timestamp) <= 0) {
                after.accept(m);
            } else {
                LOG.warn("Message {} discarded. bound={}", m, bound);
            }
        };
    }
}
