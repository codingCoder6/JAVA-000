package java0.netty1;

/**
 * @Author: 王毅兵
 * @Date: 2020-10-28 21:04
 * @Description:
 */
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
