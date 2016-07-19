import org.junit.Test;

/**
 * Created by bl04559 on 2016/7/18.
 */
public class AIOTest {
    private final static int port = 30001;
    private final static String host = "localhost";
   // @Test
    public static void testServer() {
        AIOServer server = new AIOServer(port);
        server.run();
    }

    @Test
    public void testClient() {
        AIOClient client = new AIOClient(host, port);
        client.run();
        String msg = "hello world";
        client.write(msg.getBytes());
    }

    @Test
    public static void testWebServer() {
        AioWebServer server = new AioWebServer(port);
        new Thread(server).start();
    }

    public static void main(String[] args) {
        //testServer();
        testWebServer();
    }
}
