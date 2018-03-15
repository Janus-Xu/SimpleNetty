package com.janus.netty.com.janus.netty.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author janus
 * class SimpleClientHandler
 * created On 2018/3/14 14:58
 * description
 */
public class SimpleClientHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(SimpleClientHandler.class);
    public static volatile ScheduledExecutorService e = Executors.newSingleThreadScheduledExecutor();

    /**
     * 客户端发送心跳数据内容
     */
    public static final String HEARTBEAT = "$HB$";

    /**
     * heartbeat_interval 心跳间隔
     */
    public static long HEARTBEAT_INTERVAL = 10;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("激活时间是："+new Date());
        System.out.println("HeartBeatClientHandler channelActive");
        ctx.fireChannelActive();

        System.out.println("isShutdown-"+e.isShutdown()+"isTerminated-"+e.isTerminated());
        if (e.isShutdown()){
        e.scheduleAtFixedRate(() -> {
            //定时发送心跳
            this.sendMessage(ctx,HEARTBEAT);
        }, 2, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
        }
    }

    /**
     * @description 发送数据封装方法
     * @param ctx
     * @param msg
     */
    private void sendMessage(ChannelHandlerContext ctx, String msg) {
        logger.info("send msg:" + msg);
        ctx.writeAndFlush(Unpooled.copiedBuffer((msg + "\r\n").getBytes()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("停止时间是："+new Date());
        System.out.println("HeartBeatClientHandler channelInactive");
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;
        System.out.println(message);
        if (message.equals("Heartbeat")) {
            ctx.write("has read message from server");
            ctx.flush();
        }
        ReferenceCountUtil.release(msg);
    }
}