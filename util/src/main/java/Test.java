import java.nio.ByteBuffer;
import java.util.*;

public class Test {

    public static void printByteBufferAsHex(ByteBuffer buffer) {
        int originalPosition = buffer.position();
        buffer.rewind();
        int count = 0;
        while (buffer.hasRemaining()) {
            count += 1;
            byte b = buffer.get();
            System.out.printf("%02X ", b);
            if (count % 16 == 0) {
                System.out.println("");
            }
        }

        System.out.println();
        buffer.position(originalPosition);
    }

    public static void testProto() {
        // TODO argLength > args.length will raise error
        DfsProto dp = new DfsProto("mkdir", "/tmp", (short)0);

        ByteBuffer dpEncoded = dp.encode();

        printByteBufferAsHex(dpEncoded);

        DfsProto newDp = DfsProto.decode(dpEncoded);

        System.out.println("command: " + newDp.command() + " args: " + newDp.args());
    }

    public static void testParseServerConfig() {
        List<ServerConfig> servers = JsonParser.parseServerConfig("ServerConfig.json");

        for (ServerConfig server: servers) {
            System.out.println(server.toString());
        }
    }

    public static void main(String[] args) {
        testParseServerConfig();
    }

}
