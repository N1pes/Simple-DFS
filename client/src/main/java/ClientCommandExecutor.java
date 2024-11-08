
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.*;


public class ClientCommandExecutor implements CommandExecutor {
    private class ServerAddress {
            public String ip;
            public int port;
            ServerAddress(String ip, int port) {
                this.ip = ip;
                this.port = port;
            }
    }

    private static final short  QUERY = 0;
    private static final short  RESP = 1;
    private String              commandResult = "";
    private NIOClient           masterConnector;
    private CommandParser       parser;
    private final Scanner       scanner = new Scanner(System.in);

    private final Set<String> supportedCommands = new HashSet<>(Arrays.asList(
            "ls", "cat", "touch", "mkdir", "rm",
            "put", "get"
    ));
    
    ClientCommandExecutor(NIOClient masterConnector) {
        this.parser = new CommandParser();
        this.masterConnector = masterConnector;
    }

// ------------------------- Network IO ----------------------
    public void connect() throws IOException {
        this.masterConnector.connect();
    }

    public void close() throws IOException {
        this.masterConnector.close();
    }

    public void send(ByteBuffer data) throws IOException {
        this.masterConnector.send(data);
    }

    public ByteBuffer recv() throws IOException {
        return this.masterConnector.recv();
    }


    public void sendCommandToMaster(DfsProto packet) {
        try {
            ByteBuffer data = packet.encode();
            data.flip(); // before sending, flip ByteBuffer
            this.send(data);
        } catch (IOException e) {
            Logger.error("Client send command `" + packet.command() + "` to master error");
        }
    }

    public DfsProto recvRusltFromMaster() {
        ByteBuffer result;
        try {
            result = this.recv();
            result.flip();
            Logger.debug("Client recvRusltFromMaster() ok");
            return DfsProto.decode(result);
        } catch (IOException e) {
            Logger.error("Client receive from master error");
            return new DfsProto("invalid", "", QUERY);
        }
    }
// ------------------------- Network IO ----------------------


    @Override
    public String readCommand() {
        return scanner.nextLine();
    }

    @Override
    public void responseCommand() {
        ;
    }

    private void forwardCommand(String cmdStr, List<String>paths, boolean needDisplay) {
        String path = getStringFromList(paths);
        DfsProto packet = new DfsProto(cmdStr, path, QUERY);
        DfsProto receivedPacket;
        String response;

        Logger.debug("command: " + cmdStr + " args: " + path);

        sendCommandToMaster(packet);
        receivedPacket = recvRusltFromMaster();

        response = receivedPacket.args();
        if (needDisplay)
            System.out.println(response);
    }

    public void ls(String cmdStr, List<String>paths) {
        Logger.debug("Client ls");
        forwardCommand(cmdStr, paths, true);
    }

    public void touch(String cmdStr, List<String>paths) {
        Logger.debug("Client touch");
        forwardCommand(cmdStr, paths, false);
    }

    public void mkdir(String cmdStr, List<String>paths) {
        Logger.debug("Client mkdir");
        forwardCommand(cmdStr, paths, false);
    }

    public void rm(String cmdStr, List<String>paths) {
        Logger.debug("Client rm");
        forwardCommand(cmdStr, paths, false);
    }

    public void cat(String cmdStr, List<String>paths) {

    }

    private void putOne (String ip, int port, String path, int start, int length, int suffix) throws IOException {
        Logger.debug("Client::putOne() connecting to " + ip + ":" + String.valueOf(port));
        NIOClient dataConnector = new NIOClient(ip, port, true);
        dataConnector.connect();
        Logger.debug("Client::putOne() connecting to data server ok");

        ByteBuffer data = FileHelper.randomReadFromFile(path, start, length);

        DfsProto packet = new DfsProto("put", length, getBasePath(path), (short)0, data);
        packet.setFileIdx((short)suffix);

        Logger.debug("Client::putOne() " + data.toString());

        ByteBuffer sendData = packet.encode();
        sendData.flip();

        dataConnector.send(sendData);
        dataConnector.close();
    }

    public void put(String cmdStr, String path) {
        try {
            List<ServerAddress> dataServerAddress = new ArrayList<>();
            int fileSize = FileHelper.getFileLength(path);
            DfsProto receivedPacket;
            String response;

            Logger.debug("file size: " + String.valueOf(fileSize));
            DfsProto packet = new DfsProto(cmdStr, fileSize, path, QUERY);
            this.sendCommandToMaster(packet);
            receivedPacket = recvRusltFromMaster();
            response = receivedPacket.args();
            Logger.debug("Client::put() response: " + response);

            StringTokenizer itr = new StringTokenizer(response);
            while (itr.hasMoreTokens()) {
                String[] info = itr.nextToken().split(":");
                dataServerAddress.add(new ServerAddress(info[0], Integer.parseInt(info[1])));
            }

            // start transferring data...
            int start = 0, block = 16; // TODO dummy values for now
            int i = 0;
            for (ServerAddress server: dataServerAddress) {
                putOne(server.ip, server.port, path, start, block, i);
                start += block;
                fileSize -= block;
                i += 1;
                if (fileSize < block)
                    block = fileSize;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    public void get(String cmdStr, String path) {

    }

    public boolean isSuppored(String cmd) {
        return supportedCommands.contains(cmd);
    }

    @Override
    public void execute(String cmdStr, List<String> options, List<String> paths) {
        Logger.log(cmdStr);
        String path;

        switch (cmdStr) {
            case "ls":
                this.ls(cmdStr, paths);
                break;
            case "cat":
                this.cat(cmdStr, paths);
                break;
            case "touch":
                this.touch(cmdStr, paths);
                break;
            case "mkdir":
                this.mkdir(cmdStr, paths);
                break;
            case "rm":
                this.rm(cmdStr, paths);
                break;
            case "put":
                path = paths.get(0);
                this.put(cmdStr, path);
                break;
            case "get":
                path = paths.get(0);
                this.get(cmdStr, path);
                break;
            default:
                System.out.println("???");
                break;
        }
    }
    private String getStringFromList(List<String> paths) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < paths.size(); i++) {
            if (i != paths.size() - 1)
                result.append(paths.get(i)).append(" ");
            else
                result.append(paths.get(i));
        }

        return result.toString();
    }

    private String getBasePath(String path) {
        int idx = path.lastIndexOf('/');
        if (idx != -1) {
            return path.substring(idx + 1);
        }
        return path;
    }
}