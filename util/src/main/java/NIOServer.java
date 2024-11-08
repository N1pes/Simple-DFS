import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class NIOServer extends Thread {

    private ServerSocketChannel     serverSocketChannel;
    private Selector                selector;
    private String                  receivedData = "";
    private static final BlockingQueue<ClientInfo> clientQueue = new LinkedBlockingQueue<>();
    private int                     listenPort;
    private IOCallback              readCallback;
    private Map<SocketChannel, ByteBuffer> clientBuffers;

    NIOServer(int port) throws IOException {
        listenPort = port;
        clientBuffers = new HashMap<>();
    }

    NIOServer(int port, IOCallback readCallback) throws IOException {
        listenPort = port;
        clientBuffers = new HashMap<>();
        this.readCallback = readCallback;
    }

    void setReadCallback(IOCallback readCallback) {
        this.readCallback = readCallback;
    }

    void startServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(listenPort));
            serverSocketChannel.configureBlocking(false); // non-blocking

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run()  {
        Logger.debug("running server...");
        startServer();
        loop();
    }

    private void registerWrite(SocketChannel clientSocket, ByteBuffer buffer) {
        clientBuffers.put(clientSocket, buffer);
        try {
            clientSocket.register(selector, SelectionKey.OP_WRITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeRequest(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = clientBuffers.get(clientChannel);

        Logger.log("NIOServer::writeRequest() " + buffer.toString());
        buffer.flip();
        Logger.log("NIOServer::writeRequest() " + buffer.toString());

        if (buffer != null && buffer.hasRemaining()) {
            Logger.debug("NIOServer::writeRequest() sending");
            clientChannel.write(buffer);
        }

        if (!buffer.hasRemaining()) {
            clientChannel.register(selector, SelectionKey.OP_READ);
            clientBuffers.remove(clientChannel);
            Logger.debug("writeRequest() Response sent ok: " + clientChannel.getRemoteAddress());
        }
    }

    private void readRequest(SelectionKey key) throws InterruptedException {
        Logger.debug("NIOServer(): enter readRequest");
        int bytesRead;
        SocketChannel clientSocket = (SocketChannel) key.channel();
        ByteBuffer buff = ByteBuffer.allocate(1024);
        ByteBuffer resultData;
        try {
            bytesRead = clientSocket.read(buff);
            if (bytesRead == -1) {
                Logger.log("Client closed");
                key.cancel();
                clientSocket.close();
                return;
            }
            if (this.readCallback != null) {
                Logger.debug("NIOServer(): enter readCallback");
                buff.flip();
                resultData = readCallback.onReceive(buff);
                registerWrite(clientSocket, resultData);
            } else {
                registerWrite(clientSocket, ByteBuffer.wrap("dummy resp from NIOServer".getBytes()));
            }

        } catch (IOException e) {
            System.err.println("Client disconnected unexpectedly.");
        }
    }

    public ClientInfo getCurrntClient() throws InterruptedException {
        return clientQueue.take();
    }

    public void sendData(SocketChannel clientSocket, String data) throws IOException {
        clientSocket.write(ByteBuffer.wrap(data.getBytes()));
    }

    public void loop() {
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> itr = selectedKeys.iterator();
                while (itr.hasNext()) {
                    SelectionKey key = itr.next();
                    itr.remove();
                    if (key.isAcceptable()) {
                        SocketChannel s = serverSocketChannel.accept();
                        s.configureBlocking(false);
                        s.register(selector, SelectionKey.OP_READ);
                        Logger.success("Accepted Client: " + s.getRemoteAddress());
                        continue;
                    }
                    if (key.isReadable()) {
                        readRequest(key);
                        continue;
                    }
                    if (key.isWritable()) {
                        writeRequest(key);
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String byteBufferToString(ByteBuffer b) {
        String s = StandardCharsets.UTF_8.decode(b).toString();
        return s;
    }


}
