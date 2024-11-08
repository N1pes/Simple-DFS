import java.io.IOException;
import java.util.Scanner;

public class Client {
    private static short            QUERY = 0;
    private String                  remoteIp;
    private int                     remotePort;
    private ClientCommandExecutor   commandExecutor;
    private CommandEventLoop        commandLoop;

    Client(String ip, int port, boolean isBlock) throws IOException {
        remoteIp = ip;
        remotePort = port;
        commandExecutor = new ClientCommandExecutor(new NIOClient(remoteIp, remotePort, true));
        commandLoop = new CommandEventLoop(commandExecutor);
    }

    public void close() throws IOException {
        this.commandExecutor.close();
    }

    public void loop() {
        commandLoop.loop();
    }

    public void connectToMaster() {
        try {
            this.commandExecutor.connect();
        } catch (IOException e) {
            Logger.error("Client connect to " + remoteIp + ":" + String.valueOf(remotePort) + " failed");
        }
    }

}
