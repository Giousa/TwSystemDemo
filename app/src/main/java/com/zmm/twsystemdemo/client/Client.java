package com.zmm.twsystemdemo.client;

import com.zmm.twsystemdemo.handler.ClientHandler;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Description:
 * Author:Giousa
 * Date:2017/2/9
 * Email:65489469@qq.com
 */
public class Client {
    private NioEventLoopGroup workGroup = new NioEventLoopGroup(4);
    private Channel channel;
    private Bootstrap bootstrap;
    private String hostIp;
    private ChannelFuture mChannelFuture;

    private ClientListener mClientListener;

    public void setClientListener(ClientListener clientListener) {
        mClientListener = clientListener;
    }

    public interface ClientListener{
        void onClientListener(String msg,int flag);
    }

    public Client(String hostIp) {
        this.hostIp = hostIp;
    }

    public void sendData(String msg) throws Exception {

        URI uri = new URI("http://192.168.43.1:8844");
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                uri.toASCIIString(), Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));

        // 构建http请求
        request.headers().set(HttpHeaders.Names.HOST, hostIp);
        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());
        // 发送http请求
        mChannelFuture.channel().write(request);
        mChannelFuture.channel().flush();
//        mChannelFuture.channel().closeFuture().sync();
    }

    public void start() {

        if (channel != null && channel.isActive()) {
            return;
        }

        try {
            bootstrap = new Bootstrap();
            bootstrap.group(workGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                    ch.pipeline().addLast(new HttpResponseDecoder());
                    // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                    ch.pipeline().addLast(new HttpRequestEncoder());
                    ClientHandler clientHandler = new ClientHandler();
                    clientHandler.setClientHandlerListener(new ClientHandler.ClientHandlerListener() {
                        @Override
                        public void onClientHandlerListener(int flag) {
                            if(flag == 1){
                                if(mClientListener != null){
                                    mClientListener.onClientListener("和机顶盒的连接已断开",0);
                                }
                            }
                        }
                    });
                    ch.pipeline().addLast(clientHandler);
                }
            });

            doConnect();

        } catch (Exception e) {
            if(mClientListener != null){
                mClientListener.onClientListener("机顶盒WIFI连接失败",0);
            }
            e.printStackTrace();
        }
    }

    public void doConnect() {
        if (channel != null && channel.isActive()) {
            return;
        }

        try {
            mChannelFuture = bootstrap.connect(hostIp, 8844).sync();
            mChannelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        channel = channelFuture.channel();
                        if(mClientListener != null){
                            mClientListener.onClientListener("恭喜你，连接成功！！！",1);
                        }
                        System.out.println("Connect to server successfully!");
                    } else {
                        System.out.println("Failed to connect to server, try connect after 2s");
                        if(mClientListener != null){
                            mClientListener.onClientListener("正在尝试连接...",2);
                        }
                        channelFuture.channel().eventLoop().schedule(new Runnable() {
                            @Override
                            public void run() {
                                doConnect();
                            }
                        },2, TimeUnit.SECONDS);
                    }
                }
            });
        } catch (InterruptedException e) {
            System.out.println("---连接异常---");
            if(mClientListener != null){
                mClientListener.onClientListener("连接异常发生异常",3);
            }
            e.printStackTrace();
        }


    }


}
