import java.io.*;


public class MasterServer extends NIOServer {
    private final MasterCommandExecutor       commandExecutor;
    private final NIOClient                   metaConnector;
    private final CommandEventLoop            commandEventLoop;

    // ------------------ MetaServer Interaction ------------------
    public void metaServerConnect() throws IOException {
        metaConnector.connect();
    }

    public void metaServerSend(String msg) throws IOException {
        metaConnector.send(msg);
    }

    // ------------------ MetaServer Interaction ------------------

    public String clientGetCommandResult() {
        return commandExecutor.getCommandResult();
    }


    public void closeConnector() throws IOException {
        metaConnector.close();
    }

    MasterServer(int port) throws IOException {
        super(port);
        metaConnector = new NIOClient("127.0.0.1", 9998, true);
        commandExecutor = new MasterCommandExecutor(metaConnector);
        setReadCallback(commandExecutor);
        commandEventLoop = new CommandEventLoop(commandExecutor);
    }

    // start command loop on a thread
    public void startCommandLoop() {
        commandEventLoop.start();
    }
}
