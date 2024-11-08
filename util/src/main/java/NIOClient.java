import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;

public class NIOClient {
    private  SocketChannel socketChannel;
    private  String        ip;
    private  int           port;

    NIOClient(String ip, int port, boolean isBlock) throws  IOException {
        this.ip = ip;
        this.port = port;
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(isBlock);
    }

    public void connect() throws IOException{
        socketChannel.connect(new InetSocketAddress(ip, port));
        while (!socketChannel.finishConnect()) ; // wait for connection
        Logger.success("connection is successful");
    }

    public void send(String msg) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(msg.length());
        buffer.put(msg.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        socketChannel.write(buffer);
    }

    public void send(ByteBuffer buffer) throws IOException {
        socketChannel.write(buffer);
    }

    public ByteBuffer recv() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = -1;
        bytesRead = socketChannel.read(buffer);
        if (bytesRead == -1) {
            return buffer;
        }
        System.out.println("NIOClient recv " + String.valueOf(bytesRead) + " bytes");
        return buffer;
    }

    public void close() throws IOException {
        socketChannel.close();
    }
}
