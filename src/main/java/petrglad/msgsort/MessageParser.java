package petrglad.msgsort;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * Handles a server-side channel.
 */
public class MessageParser extends ChannelInboundHandlerAdapter {

    private final Consumer<Message> queue;

    public MessageParser(Consumer<Message> queue) {
        this.queue = queue;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            final String messageStr = ((ByteBuf) msg).toString(Charset.defaultCharset());
            Message message = new Message(messageStr);
            System.out.println("Got " + message); // TODO Add logger
            queue.accept(message);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace(); // TODO Add logger
        ctx.close();
    }
}