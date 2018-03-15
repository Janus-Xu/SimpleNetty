package com.janus.netty.client;

import com.janus.netty.api.ConnectionWatchdog;
import io.netty.util.HashedWheelTimer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

/**
 * @author janus
 *         class SimpleClient
 *         created On 2018/3/14 14:53
 *         description
 */
public class SimpleClient {

    protected final HashedWheelTimer timer = new HashedWheelTimer();

    private Bootstrap boot;

    private final com.janus.netty.client.ConnectorIdleStateTrigger idleStateTrigger = new com.janus.netty.client.ConnectorIdleStateTrigger();

    public void connect(int port, String host) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();

        boot = new Bootstrap();
        boot.group(group).channel(NioSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO));

        final ConnectionWatchdog watchdog = new ConnectionWatchdog(boot, timer, port,host, true) {

            public ChannelHandler[] handlers() {
                return new ChannelHandler[] {
                        this,
                        new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS),
                        idleStateTrigger,
                        new StringDecoder(),
                        new StringEncoder(),
                        new SimpleClientHandler()
                };
            }
        };

        ChannelFuture future;
        //进行连接
        try {
            synchronized (boot) {
                boot.handler(new ChannelInitializer<Channel>() {

                    //初始化channel
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(watchdog.handlers());
                    }
                });

                future = boot.connect(host,port);
            }

            // 以下代码在synchronized同步块外面是安全的
            future.sync();
        } catch (Throwable t) {
            throw new Exception("connects to fails", t);
        }
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int port = 4001;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        new SimpleClient().connect(port, "127.0.0.1");
    }
}
