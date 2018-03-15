package com.janus.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author janus
 * class SimpleServerHandler
 * created On 2018/3/14 14:45
 * description
 */
public class SimpleServerHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(SimpleServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("server channelRead..");
        System.out.println(ctx.channel().remoteAddress() + "->Server :" + msg.toString());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.error("Client has a error,Error cause:"+cause.getCause());
        ctx.close();
    }

    /**
     * method
     * description 心跳检测机制，一段时间未进行读操作，与客户端断开连接
     * @author janus
     * created on 2018/3/14 15:30
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            //未进行读操作
            if (state == IdleState.READER_IDLE) {
                throw new Exception("idle exception,No HeartBeat Data Reading");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
