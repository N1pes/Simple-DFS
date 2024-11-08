import java.io.IOException;

public class DataServerMain {

    public static void main(String[] args) throws IOException {
        Logger.setLevel(Logger.DEBUG_LEVEL);
        DataServer dataServer = new DataServer(9997, 9996);
        dataServer.startAll();
    }
}
