package petrglad.msgsort;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpRequest;

import java.util.function.Consumer;

public class NettyHttpClient {
    private final ChannelHandler httpHandler;

    public NettyHttpClient(Consumer<HttpRequest> requestConsumer, ChannelHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    public void run() {
        final EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new HttpClientCodec(),
                                    new HttpContentDecompressor(),
                                    httpHandler);
                        }
                    });
        } finally {
            group.shutdownGracefully();
        }
    }
}
