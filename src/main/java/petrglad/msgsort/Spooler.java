package petrglad.msgsort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Invokes given task periodically.
 */
public class Spooler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Spooler.class);

    private volatile boolean isRunning = true;
    private final long delayMillis;
    private final Runnable task;

    public Spooler(Runnable task, long delayMillis) {
        this.task = task;
        this.delayMillis = delayMillis;
    }

    public void shutdown() {
        this.isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                task.run();
            } catch (Exception e) {
                LOG.error("Task error", e); // Keep running
            }
            synchronized (this) {
                try {
                    wait(delayMillis);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void start() {
        Thread t = new Thread(this);
        t.setName("Spooler d=" + delayMillis + " r=" + task.getClass().getSimpleName());
        t.setDaemon(true);
        t.start();
    }
}
