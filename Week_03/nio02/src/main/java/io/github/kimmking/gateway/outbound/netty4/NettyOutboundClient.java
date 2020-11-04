package io.github.kimmking.gateway.outbound.netty4;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.net.URISyntaxException;

public class NettyOutboundClient {

    private String proxyServer;

    private String host;
    private int port;
    private URI uri;

    public NettyOutboundClient(String proxyServer) {
        this.proxyServer = proxyServer;
        try {
            uri = new URI(proxyServer);
            this.host = uri.getHost();
            this.port = uri.getPort();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void connect(ChannelHandlerContext ctx, FullHttpRequest fullRequest) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpResponseDecoder());
                            pipeline.addLast(new HttpRequestEncoder());
                            pipeline.addLast(new NettyInboundHandler(ctx, fullRequest));
                        }
                    });
            if (port == -1) {
                port = 80;
            }
            ChannelFuture f = bootstrap.connect(host, port).sync();
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toASCIIString());
            request.headers().set("Content-Length", request.content().readableBytes());
            // 将自定义的请求头加进去
            request.headers().add(fullRequest.headers());
            f.channel().writeAndFlush(request);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
