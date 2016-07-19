import java.io.IOException;
import java.net.InetSocketAddress;
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
 * Created by timely on 2016/7/18.
 */
public class AioWebServer implements Runnable {
    private AsynchronousChannelGroup asyncChGroup;
    private AsynchronousServerSocketChannel server;
    private int port;
    private String monitor = "monitor";

    public AioWebServer(int port) {
        this.port = port;
    }

    private void initBind() throws IOException {
        ExecutorService excutor = Executors.newFixedThreadPool(30);
        asyncChGroup = AsynchronousChannelGroup.withThreadPool(excutor);
        server = AsynchronousServerSocketChannel.open(asyncChGroup).bind(new InetSocketAddress(port));
    }

    public void run() {
        try {
            initBind();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //接收
        server.accept(server, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {
            String str = "<html><head><title>aio web server</title></head><body><p>this is a aio aio web server </p></body></html>";
            String CRLF = "\r\n";
            // 响应头的参数
            String serverLine = "Server:a base on aio web server";
            String statusLine = "HTTP/1.1 200 OK" + CRLF;
            String contentTypeLine = "Content-type:text/html"
                    + CRLF;
            String contentLengthLine = "Content-Length:" + str.length()
                    + CRLF;

            public void completed(AsynchronousSocketChannel result, AsynchronousServerSocketChannel attachment) {

                StringBuilder content = new StringBuilder(statusLine);
                content.append(serverLine)
                        .append(contentTypeLine)
                        .append(contentLengthLine)
                        .append(CRLF)
                        .append(str);
                writeChannel(result, content.toString());
                try {
                    result.shutdownOutput();
                    result.shutdownInput();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    result.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                attachment.accept(attachment, this);
            }

            public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {

            }

            public void writeChannel(
                    AsynchronousSocketChannel channel, String s) {
                Future<Integer> future = channel.write(ByteBuffer
                        .wrap(s.getBytes()));

                try {
                    future.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            synchronized (monitor) {
                monitor.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        synchronized (monitor) {
            monitor.notify();
        }
    }

}
