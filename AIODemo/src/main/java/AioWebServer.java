import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by bl04559 on 2016/7/18.
 */
public class AioWebServer implements Runnable {
    private final static Logger log = Logger.getLogger(AioWebServer.class);
    private AsynchronousChannelGroup asyncChGroup;
    private AsynchronousServerSocketChannel server;
    private int port;
    private String monitor = "monitor";

    public AioWebServer(int port) {
        this.port = port;
    }

    private void initBind() throws IOException {
        log.info("init web server.....");
        ExecutorService excutor = Executors.newFixedThreadPool(30);
        asyncChGroup = AsynchronousChannelGroup.withThreadPool(excutor);
        server = AsynchronousServerSocketChannel.open(asyncChGroup).bind(new InetSocketAddress(port));
    }

    public void run() {
        try {
            initBind();
        } catch (IOException e) {
            log.error("初始化失败", e);
        }
        listen();
    }

    private final static String head;
    private final static   String CRLF = "\r\n";

    static {
        // 响应头的参数
        String serverLine = "Server:a base on aio web server";
        String statusLine = "HTTP/1.1 200 OK" + CRLF;
        String contentTypeLine = "Content-type:text/html"
                + CRLF;
        String contentLengthLine = "Content-Length:" + 100
                + CRLF;
        StringBuilder content = new StringBuilder(statusLine);
        content.append(serverLine)
                .append(contentTypeLine)
                .append(contentLengthLine)
                .append(CRLF);
        head = content.toString();
    }
    private void listen() {
        log.info("wait request...");
        //接收
        server.accept(server, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
            String str = "<html><head><title>aio web server</title></head><body><p>this is a aio aio web server </p></body></html>";
            public void completed(AsynchronousSocketChannel client, AsynchronousServerSocketChannel attachment) {
                StringBuilder content = new StringBuilder(head);
                content.append(str);
                try {
                    log.info("远程地址："+((InetSocketAddress)client.getRemoteAddress()).getHostName());
                    client.setOption(StandardSocketOptions.TCP_NODELAY, true);
                    client.setOption(StandardSocketOptions.SO_SNDBUF, 1024);
                    client.setOption(StandardSocketOptions.SO_RCVBUF, 1024);
                    if (client.isOpen()) {
                        writeChannel(client, content.toString());
                    }
                    client.shutdownOutput();
                    client.shutdownInput();
                } catch (IOException e) {
                    log.error("shutdown 异常", e);
                }finally {
                    try {
                        client.close();
                    } catch (IOException e) {
                        log.error("客户端关闭异常", e);
                    }
                    //继续监听
                    log.info("wait request...");
                    attachment.accept(attachment, this);
                }
            }

            public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
                log.error(exc);
                log.info("wait request...");
                attachment.accept(attachment, this);
            }

            /**
             * 响应
             * @param channel
             * @param s
             */
            public void writeChannel(
                    AsynchronousSocketChannel channel, String s) {
                Future<Integer> future = channel.write(ByteBuffer
                        .wrap(s.getBytes()));

                try {
                    future.get();
                } catch (InterruptedException e) {
                    log.error(e);
                } catch (ExecutionException e) {
                    log.error(e);
                }
            }
        });
        try {
            synchronized (monitor) {
                monitor.wait();
            }
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    public void shutdown() {
        synchronized (monitor) {
            monitor.notify();
        }
    }

}
