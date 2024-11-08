import java.io.IOException;

public class MasterMain {


    public static void main(String[] args) throws IOException, InterruptedException {
        Logger.setLevel(Logger.DEBUG_LEVEL);

        MasterServer master = new MasterServer(9999);

        master.start(); // start server on a thread
        master.startCommandLoop();

        // master.metaServerConnect();
    }
}

