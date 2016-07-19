import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by timely on 2016/7/18.
 */
public class Util {
    public static String buff2String(ByteBuffer buffer, Charset charset){
        byte[] bytes;
        if (buffer.hasArray()) {
            bytes = buffer.array();
        } else {
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
        }
        return new String(bytes, charset);
    }
}
