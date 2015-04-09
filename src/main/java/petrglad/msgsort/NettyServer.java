package petrglad.msgsort;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Server implementation based on Netty
 * (See https://github.com/netty/netty/wiki/User-guide-for-4.x)
 */
public class NettyServer {

    private static final Logger LOG = LoggerFactory.getLogger(NettyServer.class);

    private final int port;
    private final Supplier<ChannelHandler[]> handlersSupplier;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    public NettyServer(int port, Supplier<ChannelHandler[]> handlersSupplier) {
        this.port = port;
        this.handlersSupplier = handlersSupplier;
    }

    public ChannelFuture start() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(handlersSupplier.get());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true); // Should detect unresponsive clients

        // Bind and start to accept incoming connections.
        ChannelFuture f = b.bind(port).sync();
        return f.channel().closeFuture();
    }

    public void shutdown() {
        LOG.info("Shutting down server on port {}", port);
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}