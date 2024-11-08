import java.io.IOException;
import java.util.*;

public class MetaServer extends NIOServer {

    static private class DataServerInfo {
        int             port;
        long            diskUsage;
        String          ip;

    }

    private int                 port;
    private int                 numDataServers;
    List<DataServerInfo>        dataServers;
    MetaServerEventHandler      eventHandler;

    // ----------------------- DataServer IP Request -----------------------



    // ----------------------- DataServer IP Request -----------------------


    MetaServer(int port) throws IOException {
        super(port);
        dataServers = new ArrayList<>();
        eventHandler = new MetaServerEventHandler();
        setReadCallback(eventHandler);
    }
}
