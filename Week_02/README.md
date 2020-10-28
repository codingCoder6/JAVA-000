### 周四 10.22

#### 1、使用 GCLogAnalysis.java 自己演练一遍串行/并行/CMS/G1的案例。

#### 串行GC

java -XX:+UseSerialGC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

日志如下：

**minor GC**

```
2020-10-25T09:54:49.362+0800: 0.243: [GC (Allocation Failure) 2020-10-25T09:54:49.363+0800: 0.243: [DefNew: 139776K->17471K(157248K), 0.0244975 secs] 139776K->50057K(506816K), 0.0246726 secs] [Times: user=0.02 sys=0.03, real=0.03 secs] 
```

第一次GC 2020-10-25T09:54:49.362+0800: 0.243: [GC (Allocation Failure) 2020-10-25T09:54:49.363+0800: 0.243: [DefNew: 139776K->17471K(157248K), 0.0244975 secs] 139776K->50057K(506816K), 0.0246726 secs] [Times: user=0.02 sys=0.03, real=0.03 secs] 

年轻代139776K->17471K回收了约122m,堆内存总共139776K->50057K约回收了89m,说明约有122-89=33m从年轻代晋升到了老年代,用时30ms.分析结论：这次GC后的老年代使用量是，50057k-17471k ,约33m。而GC暂停时间是30ms

**Full GC**

```
2020-10-25T09:54:49.626+0800: 0.506: [GC (Allocation Failure) 2020-10-25T09:54:49.626+0800: 0.506: [DefNew: 157244K->157244K(157248K), 0.0000288 secs]2020-10-25T09:54:49.626+0800: 0.506: [Tenured: 298822K->269823K(349568K), 0.0290440 secs] 456067K->269823K(506816K), [Metaspace: 2715K->2715K(1056768K)], 0.0292271 secs] [Times: user=0.03 sys=0.00, real=0.03 secs] 
```

年轻代157244K->157244K(157248K)，放满了，且没有回收。[Tenured: 298822K->269823K(349568K) ，老年代只回收了约3m。

[Metaspace: 2715K->2715K(1056768K)], 0.0292271 secs] 元空间没有回收。堆空间456067K->269823K(506816K）总共回收了19m，用时30ms。GC后老年代的使用率仍达到了269823/298822 = 89%，说明老年代几乎存满了，而新生代也没有空间了，所以触发了Full GC

**分析后面几次gc的日志，发现连续进行了几次full GC，且使用率都是90%以上，说明此时512m的堆内存已经不够用，很有可能会oom。**

------



#### 并行GC

```
2020-10-25T10:57:51.647+0800: [GC (Allocation Failure) [PSYoungGen: 131584K->21501K(153088K)] 131584K->43650K(502784K), 0.0073107 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
```

第一次GC年轻代[PSYoungGen: 131584K->21501K(153088K)]，131584K->43650K(502784K)，计算得知有22m晋升到老年代。说明有很多对象在创建的时候，新生代存放不下直接放进了老年代，用是10ms。

```
2020-10-25T10:57:51.879+0800: [Full GC (Ergonomics) [PSYoungGen: 23544K->0K(116736K)] [ParOldGen: 321960K->248293K(349696K)] 345505K->248293K(466432K), [Metaspace: 2715K->2715K(1056768K)], 0.0332771 secs] [Times: user=0.13 sys=0.00, real=0.03 secs]
```

年轻代 [PSYoungGen: 23544K->0K(116736K)]，老年代[ParOldGen: 321960K->248293K(349696K)]，年轻代内存将为0，老年代使用率从94%降到了70%。用时30ms。

#### CMS GC

```
2020-10-25T11:22:18.637+0800: [GC (Allocation Failure) 2020-10-25T11:22:18.637+0800: [ParNew: 139776K->17472K(157248K), 0.0107033 secs] 139776K->50016K(506816K), 0.0111477 secs] [Times: user=0.01 sys=0.11, real=0.01 secs]
```

[ParNew: 139776K->17472K(157248K), 0.0107033 secs]CMS收集器 年轻代默认搭配ParNew。第一次GC回收了89m用时10ms。

```
2020-10-25T11:22:18.822+0800: [GC (CMS Initial Mark) [1 CMS-initial-mark: 213141K(349568K)] 231022K(506816K), 0.0003361 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-25T11:22:18.822+0800: [CMS-concurrent-mark-start]
2020-10-25T11:22:18.825+0800: [CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-25T11:22:18.825+0800: [CMS-concurrent-preclean-start]
2020-10-25T11:22:18.826+0800: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-25T11:22:18.826+0800: [CMS-concurrent-abortable-preclean-start]
2020-10-25T11:22:18.840+0800: [GC (Allocation Failure) 2020-10-25T11:22:18.840+0800: [ParNew: 157078K->17469K(157248K), 0.0265150 secs] 370220K->272683K(506816K), 0.0266729 secs] [Times: user=0.20 sys=0.03, real=0.03 secs]
2020-10-25T11:22:18.883+0800: [GC (Allocation Failure) 2020-10-25T11:22:18.883+0800: [ParNew: 157245K->17470K(157248K), 0.0274531 secs] 412459K->317634K(506816K), 0.0276681 secs] [Times: user=0.08 sys=0.02, real=0.03 secs]
2020-10-25T11:22:18.927+0800: [GC (Allocation Failure) 2020-10-25T11:22:18.927+0800: [ParNew: 157246K->17466K(157248K), 0.0278358 secs] 457410K->365011K(506816K), 0.0280445 secs] [Times: user=0.05 sys=0.02, real=0.03 secs]
2020-10-25T11:22:18.955+0800: [CMS-concurrent-abortable-preclean: 0.004/0.128 secs] [Times: user=0.41 sys=0.06, real=0.13 secs]
2020-10-25T11:22:18.955+0800: [GC (CMS Final Remark) [YG occupancy: 20458 K (157248 K)]2020-10-25T11:22:18.955+0800: [Rescan (parallel) , 0.0002851 secs]2020-10-25T11:22:18.956+0800: [weak refs processing, 0.0000688 secs]2020-10-25T11:22:18.956+0800: [class unloading, 0.0002292 secs]2020-10-25T11:22:18.956+0800: [scrub symbol table, 0.0003212 secs]2020-10-25T11:22:18.956+0800: [scrub string table, 0.0001488 secs][1 CMS-remark: 347544K(349568K)] 368003K(506816K), 0.0013159 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-25T11:22:18.957+0800: [CMS-concurrent-sweep-start]
2020-10-25T11:22:18.958+0800: [CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-25T11:22:18.958+0800: [CMS-concurrent-reset-start]
2020-10-25T11:22:18.958+0800: [CMS-concurrent-reset: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
```

可以观察到，CMS回收的过程，CMS Initial Mark 初始标记，CMS-concurrent-mark 并发标记，CMS-concurrent-preclean并发预清理，CMS Final Remark 最终标记，CMS-concurrent-sweep并发清楚，CMS-concurrent-reset并发重置，CMS GC用时比较少。

把堆内存调大到4g,再运行一遍

```
C:\Users\84981\Desktop\java训练营\Week02-Java进阶训练营-JVM进阶 NIO>java -XX:+UseConcMarkSweepGC -Xms4g -Xmx4g -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
正在执行...
2020-10-25T11:50:00.543+0800: [GC (Allocation Failure) 2020-10-25T11:50:00.544+0800: [ParNew: 545344K->68095K(613440K), 0.0325131 secs] 545344K->146629K(4126208K), 0.0328120 secs] [Times: user=0.09 sys=0.14, real=0.03 secs]
2020-10-25T11:50:00.643+0800: [GC (Allocation Failure) 2020-10-25T11:50:00.643+0800: [ParNew: 613439K->68096K(613440K), 0.0427151 secs] 691973K->263246K(4126208K), 0.0428976 secs] [Times: user=0.20 sys=0.16, real=0.04 secs]
2020-10-25T11:50:00.744+0800: [GC (Allocation Failure) 2020-10-25T11:50:00.744+0800: [ParNew: 613440K->68096K(613440K), 0.0773763 secs] 808590K->386393K(4126208K), 0.0776053 secs] [Times: user=0.44 sys=0.05, real=0.08 secs]
2020-10-25T11:50:00.883+0800: [GC (Allocation Failure) 2020-10-25T11:50:00.883+0800: [ParNew: 613440K->68096K(613440K), 0.0835592 secs] 931737K->510333K(4126208K), 0.0839127 secs] [Times: user=0.47 sys=0.05, real=0.08 secs]
2020-10-25T11:50:01.029+0800: [GC (Allocation Failure) 2020-10-25T11:50:01.029+0800: [ParNew: 613440K->68094K(613440K), 0.0776495 secs] 1055677K->639543K(4126208K), 0.0779043 secs] [Times: user=0.47 sys=0.06, real=0.08 secs]
2020-10-25T11:50:01.171+0800: [GC (Allocation Failure) 2020-10-25T11:50:01.171+0800: [ParNew: 613438K->68094K(613440K), 0.0780542 secs] 1184887K->768644K(4126208K), 0.0782413 secs] [Times: user=0.44 sys=0.03, real=0.08 secs]
```

只发生了年轻代的垃圾回收，但GC停顿时间很大



### 2、使用压测工具（wrk或sb），演练gateway-server-0.0.1-SNAPSHOT.jar 示例。

**sb** **命令**

```
sb -u http://localhost:8088/api/hello -c 20 -N 60
```

**SerialGC**

```
java -jar -Xms512m -Xmx512m -XX:+UseSerialGC gateway-server-0.0.1-SNAPSHOT.jar
```

![image-20201028215411649](images\image-20201028215411649.png)

![image-20201028215505998](images\image-20201028215505998.png)

**并行GC**

```
java -jar -Xms512m -Xmx512m gateway-server-0.0.1-SNAPSHOT.jar
```

![image-20201028220006328](images\image-20201028220006328.png)

![image-20201028220030675](images\image-20201028220030675.png)

### 周六 10.24

#### 2、写一段代码，使用 HttpClient 或 OkHttp 访问 http://localhost:8801，代码提交到 

### Github。



ServerHandler.java

```
public class ServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK, Unpooled.wrappedBuffer("Hello hangzhou".getBytes()));
        // 设置响应头
        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);


    }
}
```

NettyHttpServer.java

```
public class NettyHttpServer {
    private int port;

    public NettyHttpServer(int port) {
        this.port = port;
    }

    public void run() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 创建引导对象
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec())
                                    .addLast(new ServerHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture();
        } finally {
            // 优雅关闭
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
```

Application.java

```
public class Application {
    public static void main(String[] args) {
        try {
            NettyHttpServer server = new NettyHttpServer(8001);
            server.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

HttpClient.java

```
public class HttpClient {
    public static void main(final String[] args) throws Exception {
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpGet httpget = new HttpGet("http://localhost:8001");

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());

            // Create a custom response handler
            final HttpClientResponseHandler<String> responseHandler = new HttpClientResponseHandler<String>() {

                @Override
                public String handleResponse(
                        final ClassicHttpResponse response) throws IOException {
                    final int status = response.getCode();
                    if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                        final HttpEntity entity = response.getEntity();
                        try {
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } catch (final ParseException ex) {
                            throw new ClientProtocolException(ex);
                        }
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            final String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
        }
    }
}
```

运行结果：

```
Executing request GET http://localhost:8001/
log4j:WARN No appenders could be found for logger (org.apache.hc.client5.http.impl.classic.InternalHttpClient).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
----------------------------------------
Hello hangzhou
```

