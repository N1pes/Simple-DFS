import java.nio.ByteBuffer;

public interface IOCallback {

    ByteBuffer onReceive(ByteBuffer data);

}
