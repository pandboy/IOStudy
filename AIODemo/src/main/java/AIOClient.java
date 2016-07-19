import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by bl04559 on 2016/7/18.
 */
public class AIOClient{
    private AsynchronousSocketChannel client;
    private String host;
    private int port;

    public AIOClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        try {
            process();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void process() throws IOException, ExecutionException, InterruptedException {
        this.client = AsynchronousSocketChannel.open();
        final ByteBuffer buffer = ByteBuffer.allocate(100);
        client.connect(new InetSocketAddress(host, port), buffer, new CompletionHandler<Void, ByteBuffer>() {
            public void completed(Void result, ByteBuffer attachment) {
                System.out.println(Util.buff2String(buffer, Charset.forName("UTF-8")));

            }

            public void failed(Throwable exc, ByteBuffer attachment) {

            }
        });
       // future.get().;
    }

    public void write(byte b) {
        ByteBuffer buffer = ByteBuffer.allocate(32);
        buffer.put(b);
        buffer.flip();
        client.write(buffer);
    }

    public void write(byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(32);
        buffer.put(b);
        buffer.flip();
        client.write(buffer);
    }
}
