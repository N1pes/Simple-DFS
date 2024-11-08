import java.io.IOException;


public class TestAll {


    public static void main(String[] args) throws IOException,InterruptedException  {
        Logger.setLevel(Logger.DEBUG_LEVEL);
        DataServer dataServer = new DataServer(9997, 9996);
        dataServer.startAll();

        MetaServer metaServer = new MetaServer(9998);
        metaServer.start();

        MasterServer master = new MasterServer(9999);
        master.start();
        master.startCommandLoop();

        Thread.sleep(1000);
        master.metaServerConnect();
    }
}