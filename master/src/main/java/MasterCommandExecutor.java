import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MasterCommandExecutor implements CommandExecutor, IOCallback {
    private static short        RESPONSE = 1;
    private FileTree            root;
    private String              commandResult = "";
    private NIOClient           metaConnector;
    private int                 fileSize;
    private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> commandResultQueue = new LinkedBlockingQueue<>();
    private final Set<String>   supportedCommands = new HashSet<>(Arrays.asList(
            "ls", "cat", "touch", "mkdir", "rm",
            "put", "get"
    ));

    MasterCommandExecutor(NIOClient connector) throws IOException {
        this.root = new FileTree("/", true);
        this.metaConnector = connector;
    }
    // --------------------- MetaServer IO --------------------
    void sendGetIPRequest() throws IOException{
        Logger.debug("sending ip request");
        DfsProto packet = new DfsProto("get", fileSize, "ip", (short)0);
        ByteBuffer data = packet.encode();
        data.flip();
        metaConnector.send(data);
    }

    String recvIPResponse() throws  IOException {
        String result;
        ByteBuffer resp = metaConnector.recv();
        resp.flip();
        result = byteBufferToString(resp);
        Logger.debug("recvIPResponse(): ip = " + result);
        return result;
    }
    // --------------------- MetaServer IO --------------------

    // TODO
    @Override
    public ByteBuffer onReceive(ByteBuffer data) {
        try {
            Logger.debug("onReceive Master" );
            DfsProto packet = DfsProto.decode(data);
            String command, args, cmdesult;
            fileSize = packet.fileSize();
            command = packet.command();
            args = packet.args();
            Logger.debug("Command: " + command + " " + args);
            commandQueue.add(command + " " + args);
            // wait for command to be done
            cmdesult = commandResultQueue.take();
            DfsProto sendPacket = new DfsProto("resp", cmdesult, RESPONSE);

            return sendPacket.encode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String readCommand() {
        try {
            return commandQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void responseCommand() {
        commandResultQueue.add(getCommandResult());
    }

    // -------------------- Commands ----------------------
    private String lsOne(String path) {
        StringBuilder result = new StringBuilder();

        if (!pathExistAbsolute(path)) {
            System.err.println(path + " not found");
            return result.toString();
        }

        FileTree f = root.getChild(path);
        if (f.isDirectory) {
            result = f.listChildren();
            return result.toString();
        }
        // file case
        result.append(f.name);
        return result.toString();
    }

    public void ls(List<String> paths) {
        boolean multiPath = paths.size() > 1;
        StringBuilder result = new StringBuilder();
        for (String path: paths) {
            if (multiPath)
                result.append(path + ":\n");
            result.append(lsOne(path));
        }
        commandResult = result.toString();
    }

    public void cat() {

    }

    private void touchOne(String path) {
        String parentPath, basePath;

        Logger.debug("touchOne() " + path);
        if (pathExistAbsolute(path)) {
            System.err.println(path + " already exists");
            return ;
        }
        parentPath = getParentPath(path);
        basePath = getBasePath(path);
        if (parentPath.equals(basePath)) {
            parentPath = "/";
        }

        Logger.debug("parent: " + parentPath + " base: " + basePath);
        if (!pathExistAbsolute(parentPath)) {
            System.err.println(path + " does not exist");
            return ;
        }
        FileTree f = root.getChild(parentPath);
        f.add(new FileTree(basePath, false));
    }

    public void touch(List<String> paths) {
        for (String path: paths) {
            touchOne(path);
        }
    }

    private void mkdirOne(String path) {
        String parentPath, basePath;
        Logger.debug("mkdirOne() " + path);

        if (pathExistAbsolute(path)) {
            System.err.println(path + " already exists");
            return ;
        }
        parentPath = getParentPath(path);
        basePath = getBasePath(path);
        if (parentPath.equals(basePath)) {
            parentPath = "/";
        }

        Logger.debug("parent: " + parentPath + " base: " + basePath);
        if (!pathExistAbsolute(parentPath)) {
            System.err.println(path + " does not exist");
            return ;
        }
        FileTree f = root.getChild(parentPath);
        f.add(new FileTree(basePath, true));
    }

    public void mkdir(List<String> paths) {
        for (String path: paths) {
            mkdirOne(path);
        }
    }

    private void rmOne(String path) {
        String parentPath, basePath;
        Logger.debug("rmOne() " + path);
        if (!pathExistAbsolute(path)) {
            System.err.println("rm(): " + path + " does not exist");
            return ;
        }
        parentPath = getParentPath(path);
        basePath = getBasePath(path);
        FileTree parent = root.getChild(parentPath);
        parent.remove(basePath);
    }

    public void rm(List<String> paths) {
        for (String path: paths) {
            rmOne(path);
        }
    }

    public void put() {
        Logger.debug("invoke put()");
        try {
            sendGetIPRequest();
            this.commandResult = recvIPResponse();
            Logger.debug("Put command result: " + this.commandResult);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void get() {

    }

    public boolean isSuppored(String cmd) {
        return supportedCommands.contains(cmd);
    }

    @Override
    public void execute(String cmdStr, List<String> options, List<String> paths) {
        if (cmdStr == null) {
            Logger.error("Invalid cmd from client");
            return;
        }

        switch (cmdStr) {
            case "ls":
                this.ls(paths);
                break;
            case "cat":
                this.cat();
                break;
            case "touch":
                this.touch(paths);
                break;
            case "mkdir":
                this.mkdir(paths);
                break;
            case "rm":
                this.rm(paths);
                break;
            case "put":
                this.put();
                break;
            case "get":
                this.get();
                break;
            default:
                System.err.println("??? Invalid command");
                break;
        }
    }

    public String getCommandResult() {
        String res = commandResult;
        commandResult = "";
        return res;
    }

    // --------------- utils -------------------

    // TODO 优化性能
    boolean pathExistAbsolute(String path) {
        FileTree curr = this.root;
        List<String> paths = getSplitPath(path);
        for (String p : paths) {
            Logger.debug(p);
            if (!curr.existDirectChild(p)) {
                Logger.debug("pathExistAbsolute(): " + p + " not exist ");
                return false;
            }
            curr = curr.getDirectChild(p);
        }
        return true;
    }

    private ArrayList<String> getSplitPath(String path) {
        StringTokenizer itr = new StringTokenizer(path, "/");
        ArrayList<String> splitPaths = new ArrayList<>();
        while (itr.hasMoreTokens()) {
            splitPaths.add(itr.nextToken());
        }
        return splitPaths;
    }

    private String getParentPath(String path) {
        int idx = path.lastIndexOf('/');
        if (idx != -1) {
            return path.substring(0, idx);
        }
        System.err.println("get parent path failed for " + path);
        return "";
    }

    private String getBasePath(String path) {
        int idx = path.lastIndexOf('/');
        if (idx != -1) {
            return path.substring(idx + 1);
        }
        System.err.println("get base path failed for " + path);
        return "";
    }

    String byteBufferToString(ByteBuffer b) {
        String s = StandardCharsets.UTF_8.decode(b).toString();
        return s;
    }

}