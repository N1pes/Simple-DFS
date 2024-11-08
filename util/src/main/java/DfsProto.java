import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Queue;

class DFSHeader {
    private static final String     MAGIC = "DFSP";
    public static final short       QUERY = 0;
    public static final short       RESPONSE = 1;

    private String                  magic;
    public  DfsProto.Command        command;
    public short                    mode = QUERY; // query or response
    public short                    argLength;
    public int                      fileSize;
    public short                    fileIdx;
    private final int               HEADER_SIZE = 18;

    DFSHeader(DfsProto.Command command, short mode, short argLength, int fileSize, short fileIdx) {
        this.magic = MAGIC;
        this.command = command;
        this.mode = mode;
        this.argLength = argLength;
        this.fileSize = fileSize;
        this.fileIdx = fileIdx;
    }

    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.put(magic.getBytes(StandardCharsets.UTF_8));
        buffer.putInt(command.getValue());
        buffer.putShort(mode);
        buffer.putShort(argLength);
        buffer.putInt(fileSize);
        buffer.putShort(fileIdx);
        buffer.flip();
        return buffer;
    }

    public static DFSHeader decode(ByteBuffer buffer) {
        byte[] magicBytes = new byte[4];
        buffer.get(magicBytes);
        String magic = new String(magicBytes, StandardCharsets.UTF_8);

        if (!magic.equals(MAGIC)) {
            System.err.println(magic);
            throw new IllegalArgumentException("Invalid magic value");
        }

        int commandValue = buffer.getInt();
        DfsProto.Command command = DfsProto.Command.fromValue(commandValue);

        short mode      = buffer.getShort();
        short argLength = buffer.getShort();
        int fileSize    = buffer.getInt();
        short idx       = buffer.getShort();

        return new DFSHeader(command, mode, argLength, fileSize, idx);
    }

    public String command() {
        return this.command.getName();
    }

    public int fileSize() { return this.fileSize; }

    public void setFileSize(int fileSize) { this.fileSize = fileSize; }

    public void setFileIdx(short idx) { this.fileIdx = idx; }
}

// -------------------- header -----------------------

public class DfsProto {

    private static final int          HEADER_SIZE = 18;
    private final DFSHeader           header;

    private ByteBuffer                args;
    private ByteBuffer                data; // file contents

    // ------------------------- Command Enum -------------------------
    public enum Command {
        RESP(0, "resp"),
        LS(1, "ls"),
        TOUCH(2, "touch"),
        MKDIR(3, "mkdir"),
        CAT(4, "cat"),
        RM(5, "rm"),
        PUT(6, "put"),
        GET(7, "get"),
        INVALID(-1, "invalid");

        private final int value;
        private final String name;

        Command(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public static Command fromValue(int value) {
            for (Command command : Command.values()) {
                if (command.getValue() == value) {
                    return command;
                }
            }
            return INVALID;
        }

        public static Command fromName(String name) {
            for (Command command : Command.values()) {
                if (command.getName().equalsIgnoreCase(name)) {
                    return command;
                }
            }
            return INVALID;
        }
    }
    // ------------------------- Command Enum -------------------------

    DfsProto(String command, String args, short mode) {
        header = new DFSHeader(Command.fromName(command), mode, (short)args.length(), 0, (short)0);
        this.args = ByteBuffer.wrap(args.getBytes(StandardCharsets.UTF_8));
        this.data = null;
    }

    DfsProto(String command, int fileSize, String args, short mode) {
        header = new DFSHeader(Command.fromName(command), mode, (short) args.length(), fileSize, (short)0);
        this.args = ByteBuffer.wrap(args.getBytes(StandardCharsets.UTF_8));
        this.data = ByteBuffer.allocate(fileSize);
    }

    DfsProto(String command, int fileSize, String args, short mode, ByteBuffer data) {
        header = new DFSHeader(Command.fromName(command), mode, (short) args.length(), fileSize, (short)0);
        this.args = ByteBuffer.wrap(args.getBytes(StandardCharsets.UTF_8));
        this.data = data;
    }

    DfsProto(DFSHeader header) {
        this.header = header;
    }

    public ByteBuffer encode() {
        ByteBuffer finalBuffer;
        ByteBuffer headerBuffer = header.encode();

        if (header.mode == DFSHeader.QUERY) {
            ByteBuffer argsBuffer = ByteBuffer.allocate(header.argLength);
            ByteBuffer dataBuffer = ByteBuffer.allocate(header.fileSize);

            args.get(argsBuffer.array());

            if (data != null && header.fileSize != 0) {
                Logger.log(data.toString());
                Logger.log(dataBuffer.toString());
                data.get(dataBuffer.array());
            }

            finalBuffer = ByteBuffer.allocate(HEADER_SIZE + header.argLength + header.fileSize);
            finalBuffer.put(headerBuffer);
            finalBuffer.put(argsBuffer);
            finalBuffer.put(dataBuffer);

            return finalBuffer;
        }
        if (header.mode == DFSHeader.RESPONSE) {
            ByteBuffer respBuffer = ByteBuffer.allocate(header.argLength);
            args.get(respBuffer.array());
            finalBuffer = ByteBuffer.allocate(HEADER_SIZE + header.argLength);
            finalBuffer.put(headerBuffer);
            finalBuffer.put(respBuffer);
            return finalBuffer;
        }
        finalBuffer = ByteBuffer.allocate(0);
        return finalBuffer;
    }

    public static DfsProto decode(ByteBuffer buffer) {
//        printByteBufferAsHex(buffer);
        DFSHeader header = DFSHeader.decode(buffer);
        if (header.mode == DFSHeader.QUERY) {
            ByteBuffer args = ByteBuffer.allocate(header.argLength);
            ByteBuffer data;

            DfsProto proto = new DfsProto(header);
            buffer.get(args.array());
            if (header.fileSize == 0) {
                data = null;
            } else {
                data = ByteBuffer.allocate(header.fileSize);
                buffer.get(data.array());
            }
            proto.args = args;
            proto.data = data;
            return proto;
        }
        // if mode is respone, we take arglength as response length
        if (header.mode == DFSHeader.RESPONSE) {
            ByteBuffer resp = ByteBuffer.allocate(header.argLength);
            DfsProto proto = new DfsProto(header);
            buffer.get(resp.array());
            proto.args = resp;
            proto.data = null;
            return proto;
        }
        return new DfsProto("Invalid", "", (short)0);

    }

    public void setData(ByteBuffer data) { this.data = data; }

    public String args() {
        return byteBufferToString(this.args);
    }

    public String command() {
        return this.header.command();
    }

    public ByteBuffer data() { return this.data; }

    public int fileSize() {
        return this.header.fileSize();
    }

    public short fileIdx() { return this.header.fileIdx; }

    public void setFileIdx(short idx) { this.header.setFileIdx(idx); }

    private String byteBufferToString(ByteBuffer b) {
        String s = StandardCharsets.UTF_8.decode(b).toString();
        return s;
    }

    public static void printByteBufferAsHex(ByteBuffer buffer) {
        int originalPosition = buffer.position();
        buffer.rewind();
        int count = 0;
        while (buffer.hasRemaining()) {
            count += 1;
            byte b = buffer.get();
            System.out.printf("%02X ", b);
            if (count % 16 == 0) {
                System.out.println("");
            }
        }

        System.out.println();
        buffer.position(originalPosition);
    }
}
