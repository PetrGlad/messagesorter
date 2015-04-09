package petrglad.msgsort;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * Handles a server-side channel.
 */
public class MessageParser extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(MessageParser.class);

    private final Consumer<Message> queue;

    public MessageParser(Consumer<Message> queue) {
        this.queue = queue;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            final String messageStr = ((ByteBuf) msg).toString(Charset.defaultCharset());
            Message message = new Message(messageStr);
            LOG.info("Got {}", message);
            queue.accept(message);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("Error in message parser {}", cause);
        ctx.close();
    }
}