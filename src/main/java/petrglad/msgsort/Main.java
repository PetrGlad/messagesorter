package petrglad.msgsort;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LineBasedFrameDecoder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

import static petrglad.msgsort.MessageSender.getMessageSender;
import static petrglad.msgsort.MessageSender.getMessageURIFunction;

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
                .setDefault("http://localhost:9200/incoming?timestamp=" + MessageSender.VALUE_PLACE + "&value=" + MessageSender.VALUE_PLACE);
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
            final String destUri = ns.getString(DEST_OPT);

            final Processor processor = new Processor(
                    windowLength,
                    getMessageSender(getMessageURIFunction(destUri)));
            addShutdownHook(processor::shutdown);

            final Spooler spooler = new Spooler(processor::run, 900);
            spooler.start();
            addShutdownHook(spooler::shutdown);

            final Supplier<ChannelHandler[]> handlersSupplier = () -> new ChannelHandler[]{
                    new LineBasedFrameDecoder(256, true, true),
                    new MessageParser(processor::add)};
            for (Object port : ns.getList(PORT_OPT)) {
                startNettyServer(handlersSupplier, (Integer) port);
            }

            Object wait = new Object();
            synchronized (wait) {
                wait.wait();
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        } catch (Exception e) {
            LOG.error("Exception caught on top level", e);
            System.exit(1);
        }
    }

    private static void startNettyServer(Supplier<ChannelHandler[]> handlersSupplier, int port) {
        final NettyServer nettyServer = new NettyServer(port, handlersSupplier);
        try {
            nettyServer.start();
            addShutdownHook(nettyServer::shutdown);
            LOG.info("Started server on port {}", port);
        } catch (Exception e) {
            LOG.error("Cannot start server on port " + port, e);
        }
    }

    private static void addShutdownHook(Runnable runnable) {
        Runtime.getRuntime().addShutdownHook(new Thread(runnable));
    }
}
