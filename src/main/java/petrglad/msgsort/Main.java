package petrglad.msgsort;


import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LineBasedFrameDecoder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class Main {

    public static final String PORT_OPT = "port";
    public static final String DEST_OPT = "dest";

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
        parser.addArgument("-b", "--buffer")
                .help("Defines max number of messages to accumulate in buffer.")
                .type(Integer.class)
                .setDefault(20);
        return parser;
    }

    public static void main(String[] args) {
        final ArgumentParser parser = makeCommandLineParser();
        try {
            final Namespace ns = parser.parseArgs(args);

            System.out.println(ns.getList(PORT_OPT));

            final BlockingQueue<Message> queue = new PriorityBlockingQueue<>();

            // ns.getList(PORT_OPT) // FIXME Open on all ports
            new petrglad.msgsort.NettyServer(
                    (Integer)(ns.getList(PORT_OPT).get(0)), // FIXME Open on all ports
                    () -> new ChannelHandler[]{
                            new LineBasedFrameDecoder(256, true, true),
                            new MessageParser(queue::add)})
                    .run();

            ///queue.remainingCapacity()

            System.exit(0);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        } catch (Exception e) {
            e.printStackTrace(); // TODO Add logger
        }
        System.exit(1);
    }
}
