package com.zmm.twsystemdemo.handler;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Description:
 * Author:Giousa
 * Date:2017/2/9
 * Email:65489469@qq.com
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private ClientHandlerListener mClientHandlerListener;

    public interface ClientHandlerListener{
        void onClientHandlerListener(int flag);
    }

    public void setClientHandlerListener(ClientHandlerListener clientHandlerListener) {
        mClientHandlerListener = clientHandlerListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ctx.writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("---channelInactive---");
        if(mClientHandlerListener != null){
            mClientHandlerListener.onClientHandlerListener(1);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("---channelUnregistered---");
        if(mClientHandlerListener != null){
            mClientHandlerListener.onClientHandlerListener(2);
        }
    }

}