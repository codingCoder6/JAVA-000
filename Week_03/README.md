####  本次作业

**源码链接：**https://github.com/codingCoder6/JavaCourseCodes/tree/main/02nio/nio02/src/main/java/io/github/kimmking/gateway

### 1.周四作业：整合你上次作业的 httpclient/okhttp；

使用Okhttp的方式发出请求：

OkhttpOutboundHandler.java

```
package io.github.kimmking.gateway.outbound.okhttp;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class OkhttpOutboundHandler {

    private String proxyServer;

    private OkHttpClient client;

    private ThreadPoolExecutor executor;

    public OkhttpOutboundHandler(String proxyServer) {
        this.proxyServer = proxyServer;
        RejectedExecutionHandler policy = new ThreadPoolExecutor.CallerRunsPolicy();
        int cores = Runtime.getRuntime().availableProcessors() * 2;
        long keepAliveTime = 1000;
        int queueSize = 2048;
        executor = new ThreadPoolExecutor(cores, cores, keepAliveTime, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueSize), policy);
    }

    public void handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        String url = proxyServer + fullRequest.uri();
        executor.submit(() -> execute(ctx, url, fullRequest));
    }

    private void execute(ChannelHandlerContext ctx, String url, FullHttpRequest fullRequest) {
//        创建OkHttpClient
        client = new OkHttpClient.Builder()
                .build();
        Request request = new Request.Builder().get().url(url).build();
        Response response = null;
        Call call = client.newCall(request);
        try {
            response = call.execute();
            handleResponse(ctx, response, fullRequest);
        } catch (IOException e) {
            e.printStackTrace();
            call.cancel();
        }
    }

    private void handleResponse(ChannelHandlerContext ctx, Response response, FullHttpRequest fullRequest) throws IOException {
        DefaultFullHttpResponse httpResponse = null;
        if(response.isSuccessful()){
            try {
                String body = response.body().string();
//                设置响应头
                httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body.getBytes()));
                httpResponse.headers().set("Content-Type", "application/json");
                httpResponse.headers().setInt("Content-Length", httpResponse.content().readableBytes());
            } catch (Exception e) {
                e.printStackTrace();
                httpResponse = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
            } finally {
                if (!HttpUtil.isKeepAlive(fullRequest)) {
                    ctx.write(httpResponse).addListener(ChannelFutureListener.CLOSE);
                } else {
                    ctx.write(httpResponse);
                }
                ctx.flush();
                ctx.close();
            }
        }
    }
}

```

### 2.周四作业（可选）:使用 netty 实现后端 http 访问（代替上一步骤）；

HttpInboundHandler.java

```
package io.github.kimmking.gateway.inbound;

import io.github.kimmking.gateway.filter.HttpRequestFilter;
import io.github.kimmking.gateway.filter.HttpRequestFilterImpl;
import io.github.kimmking.gateway.outbound.httpclient4.HttpOutboundHandler;
import io.github.kimmking.gateway.outbound.netty4.NettyOutboundHandler;
import io.github.kimmking.gateway.outbound.okhttp.OkhttpOutboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);
    private final String proxyServer;
    private OkhttpOutboundHandler handler;
    private NettyOutboundHandler nettyHandler;
    private HttpRequestFilter filter;

    public HttpInboundHandler(String proxyServer) {
        this.proxyServer = proxyServer;
//        OkHttp客户端请求
//        handler = new OkhttpOutboundHandler(this.proxyServer);
        //netty 作为客户端请求
        nettyHandler = new NettyOutboundHandler(proxyServer);
        filter = new HttpRequestFilterImpl();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            filter.filter(fullRequest, ctx);
            nettyHandler.handle(fullRequest, ctx);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
```

NettyOutboundHandler.java

```
package io.github.kimmking.gateway.outbound.netty4;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.net.URISyntaxException;

public class NettyOutboundHandler {

    private String proxyServer;
    private NettyOutboundClient client;

    public NettyOutboundHandler(String proxyServer) {
        this.proxyServer = proxyServer;
        client = new NettyOutboundClient(proxyServer);
    }

    public void handle(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        // 调用后端服务
        client.connect(ctx,fullRequest);
    }
}
```

NettyOutboundClient.java

```
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
```

NettyInboundHandler.java

```
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
```

### 3.周六作业：实现过滤器

HttpRequestFilter.java

```
package io.github.kimmking.gateway.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface HttpRequestFilter {
    
    void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx);

}

```

HttpRequestFilterImpl.java

```
package io.github.kimmking.gateway.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpRequestFilterImpl implements HttpRequestFilter {
    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        fullRequest.headers().set("nio","wangyibing");
    }
}
```

HttpInboundHandler.java

```
package io.github.kimmking.gateway.inbound;

import io.github.kimmking.gateway.filter.HttpRequestFilter;
import io.github.kimmking.gateway.filter.HttpRequestFilterImpl;
import io.github.kimmking.gateway.outbound.httpclient4.HttpOutboundHandler;
import io.github.kimmking.gateway.outbound.netty4.NettyOutboundHandler;
import io.github.kimmking.gateway.outbound.okhttp.OkhttpOutboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);
    private final String proxyServer;
    private OkhttpOutboundHandler handler;
    private NettyOutboundHandler nettyHandler;
    private HttpRequestFilter filter;

    public HttpInboundHandler(String proxyServer) {
        this.proxyServer = proxyServer;
//        OkHttp客户端请求
//        handler = new OkhttpOutboundHandler(this.proxyServer);
        //netty 作为客户端请求
        nettyHandler = new NettyOutboundHandler(proxyServer);
        filter = new HttpRequestFilterImpl();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            filter.filter(fullRequest, ctx);
            nettyHandler.handle(fullRequest, ctx);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
```

