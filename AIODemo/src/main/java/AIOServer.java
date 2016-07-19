import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Created by bl04559 on 2016/7/18.
 */
public class AIOServer implements Runnable{

    private int port;

    private String monitor = "monitor";

    public AIOServer(int port) {
        this.port = port;
    }

    public void run() {
        try {
            process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process() throws IOException {
        final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port));
        listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            public void completed(AsynchronousSocketChannel ch, Void attachment) {
                listener.accept(null, this);
                handle(ch);
            }
            public void failed(Throwable exc, Void attachment) {
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
    private void handle(AsynchronousSocketChannel ch) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        try {
            ch.read(byteBuffer).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        byteBuffer.flip();
        ByteBuffer message = ByteBuffer.wrap("I am get".getBytes());
        System.out.println(new Date().toString());
        System.out.printf(Util.buff2String(byteBuffer, Charset.defaultCharset()));
    }

    public void shutdown() {
        synchronized (monitor) {
            monitor.notify();
        }
    }
}
