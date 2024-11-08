import java.util.Scanner;
import java.io.*;
import java.net.*;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        Logger.setLevel(Logger.DEBUG_LEVEL);

        String  masterIp = "127.0.0.1";
        int     masterPort = 9999;

        Client client = new Client(masterIp, masterPort, true);

        client.connectToMaster();

        client.loop();

        client.close();
    }

}
