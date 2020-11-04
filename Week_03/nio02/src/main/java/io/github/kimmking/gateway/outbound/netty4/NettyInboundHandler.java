package io.github.kimmking.gateway.outbound.netty4;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class NettyInboundHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext parentCtx;
    private FullHttpRequest request;

    public NettyInboundHandler(ChannelHandlerContext ctx, FullHttpRequest fullRequest) {
        this.parentCtx = ctx;
        this.request = fullRequest;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            String body = content.content().toString(CharsetUtil.UTF_8);
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, OK, Unpooled.wrappedBuffer(body.getBytes()));
            httpResponse.headers().set("Content-Type", "application/json");
            httpResponse.headers().set("Content-Length", body.length());
            parentCtx.write(httpResponse).addListener(ChannelFutureListener.CLOSE);
            // 清空缓存区
            parentCtx.flush();
            // 关闭channel
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
