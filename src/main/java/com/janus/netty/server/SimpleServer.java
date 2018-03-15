package com.janus.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author janus
 * class SimpleServer
 * created On 2018/3/14 14:13
 * description
 */
public class SimpleServer {

    private static Logger logger = LoggerFactory.getLogger(SimpleServer.class);

    private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();

    private int port;

    public SimpleServer(int port) {
        this.port = port;
    }

    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup(4);

    /**
     * method 
     * description 
     * @author janus
     * created on 2018/3/14 14:16
     */
    public void start() {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 接收进来的连接,一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上
        EventLoopGroup workerGroup = new NioEventLoopGroup();        //处理已经被接收的连接

        try {
            ServerBootstrap b = new ServerBootstrap() // 启动NIO服务的辅助启动类
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) //  配置SocketChannel
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 事件处理类，用来处理一个最近已经接收的Channel
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 自定义分隔符数据
                            ch.pipeline().addLast(
                                    new DelimiterBasedFrameDecoder(8192, true,
                                            Unpooled.copiedBuffer("\r\n".getBytes())));
                            // 默认格式化String串
                            ch.pipeline().addLast(new StringDecoder());
                            //心跳检测5秒一次；读操作空闲秒数设定为5秒;写操作的空闲秒数;读写空闲秒数;
                            ch.pipeline().addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(idleStateTrigger);
                            ch.pipeline().addLast(new SimpleServerHandler());
                        }
                    });

            //        b.option(ChannelOption.SO_BACKLOG, 1024);
            //        b.childOption(ChannelOption.TCP_NODELAY, true);
            //        b.childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = b.bind(port).sync();
            logger.info("Server start listen at " + port);

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port =4001;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 4001;
        }
        new SimpleServer(port).start();
    }
}
