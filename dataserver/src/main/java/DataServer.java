import java.io.IOException;

public class DataServer {

    private int                 metaPort;
    private int                 dataPort;
    private NIOServer           dataServer; // transfer data
    private NIOServer           metaServer; // transfer meta info
    private DataEventHandler    dataEventHandler;

    // for test
    private NIOServer           dataServer2;
    private DataEventHandler    dataEventHandler2;

    DataServer(int metaPort, int dataPort) throws IOException {
        metaPort = metaPort;
        dataPort = dataPort;
        metaServer = new NIOServer(metaPort);
        dataServer = new NIOServer(dataPort);

        dataEventHandler = new DataEventHandler();
        dataServer.setReadCallback(dataEventHandler);

        // for test
        dataServer2 = new NIOServer(9995);
        dataEventHandler2 = new DataEventHandler();
        dataServer2.setReadCallback(dataEventHandler2);
    }

    public void startAll() {
        metaServer.start();
        dataServer.start();
        dataServer2.start();
    }

}
