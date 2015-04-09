package petrglad.msgsort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

public class Processor {
    private final BlockingQueue<Message> queue;
    private final Consumer<? super Message> sender;
    private final int windowLength;

    public Processor(int windowLength, Consumer<? super Message> sender) {
        assert windowLength >= 0;
        this.windowLength = windowLength;
        queue = new PriorityBlockingQueue<>(
                windowLength * 3,
                (a, b) -> a.timestamp.compareTo(b.timestamp));
        this.sender = new MonotonicRestriction().andThen(sender);
    }

    private void sendPendingMessages(int windowLength) {
        final Collection<Message> messages = new ArrayList<>(this.windowLength * 2);
        // XXX If client stops sending messages then last windowLength messages may stuck in the queue indefinitely.
        queue.drainTo(messages, Math.max(0, queue.size() - windowLength));
        messages.forEach(sender);
    }

    public void run() {
        sendPendingMessages(windowLength);
    }

    public void add(Message m) {
        queue.add(m);
    }

    public void shutdown() {
        sendPendingMessages(0);
    }
}
