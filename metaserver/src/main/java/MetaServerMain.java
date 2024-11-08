import java.io.IOException;

public class MetaServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        Logger.setLevel(Logger.DEBUG_LEVEL);
        MetaServer metaServer = new MetaServer(9998);
        metaServer.start();
    }
}
