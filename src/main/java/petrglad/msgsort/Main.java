package petrglad.msgsort;


import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LineBasedFrameDecoder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static final String PORT_OPT = "port";
    public static final String DEST_OPT = "dest";
    public static final String BUFFER_OPT = "buffer";

    private static ArgumentParser makeCommandLineParser() {
        final ArgumentParser parser = ArgumentParsers.newArgumentParser("MessagesSort")
                .defaultHelp(true)
                .description("Aggregate incoming messages, post them timestamp-sorted to remote server.");
        parser.addArgument("-p", "--" + PORT_OPT)
                .nargs("*")
                .type(Integer.class)
                .required(true)
                .help("Ports to listen for incoming messages.");
        parser.addArgument("-t", "--" + DEST_OPT)
                .required(true)
                .help("Target HTTP URL to post resulting messages.")
                        // XXX Hardcoded for convenience to match one in petrglad.msgsort.stub.DestinationStub
                .setDefault("http://localhost:9200/incoming");
        parser.addArgument("-b", "--" + BUFFER_OPT)
                .help("Defines max number of messages to accumulate in reorder buffer.")
                .type(Integer.class)
                .setDefault(10);
        return parser;
    }

    public static void main(String[] args) {
        final ArgumentParser parser = makeCommandLineParser();
        try {
            final Namespace ns = parser.parseArgs(args);
            final Integer windowLength = ns.getInt(BUFFER_OPT);
            final int maxWindow = windowLength * 2;
            final BlockingQueue<Message> queue = new PriorityBlockingQueue<>(
                    maxWindow, (a, b) -> a.timestamp.compareTo(b.timestamp));

            final String destUri = ns.getString(DEST_OPT);
            final AtomicReference<LocalDateTime> sentBoundary = new AtomicReference<>(LocalDateTime.now().minusYears(1));
            // TODO Ensure that all received messages are sent before shutting down
            final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
            exec.scheduleAtFixedRate(() -> {
                        final Collection<Message> messages = new ArrayList<>(maxWindow);
                        // XXX If client stops sending messages then last windowLength messages may stuck in the queue indefinitely.
                        queue.drainTo(messages, Math.max(0, queue.size() - windowLength));
                        try (AhcHttpClient httpClient = new AhcHttpClient()) {
                            for (Message m : messages) {
                                if (sentBoundary.get().compareTo(m.timestamp) < 0) { // Discard if violates ordering
                                    try {
                                        URIBuilder ub = new URIBuilder(destUri);
                                        ub.addParameter("timestamp", Messages.formatTimestamp(m.timestamp));
                                        ub.addParameter("value", Long.toString(m.value));
                                        if (httpClient.get(ub.build()).isSuccessful()) {
                                            sentBoundary.getAndUpdate(localDateTime -> Util.max(localDateTime, m.timestamp));
                                        }
                                    } catch (URISyntaxException e) {
                                        LOG.error("Destination URI '" + destUri + "' is incorrect for message '" + m + "'", e);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            LOG.error("Error sending messages", e);
                        }
                    },
                    1, 1, TimeUnit.SECONDS);

            System.out.println(ns.getList(PORT_OPT)); // FIXME Open on all ports
            new petrglad.msgsort.NettyServer(
                    (Integer) (ns.getList(PORT_OPT).get(0)), // FIXME Open on all ports
                    () -> new ChannelHandler[]{
                            new LineBasedFrameDecoder(256, true, true),
                            new MessageParser(queue::add)})
                    .run();

            System.exit(0);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        } catch (Exception e) {
            LOG.error("Exception caught on top level", e);
        }
        System.exit(1);
    }
}
