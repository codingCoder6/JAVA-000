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
