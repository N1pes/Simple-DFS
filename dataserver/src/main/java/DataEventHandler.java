import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.File;

// handle data server on receive event
public class DataEventHandler implements IOCallback {

    private Map<String, Integer>        fileRecorder = new HashMap<>();
    private String                      dataRootPath = "/DFSData";

    private String genAbsolutePath(String prefix, String name) {
        return dataRootPath + File.separator + prefix + File.separator + name;
    }

    private ByteBuffer handleClientCat(DfsProto packet) {
        try {
            String filepath = packet.args();
            String prefix = dataRootPath;
            short fileIdx = packet.fileIdx();
            String suffix = ".split" + String.valueOf(fileIdx);
            filepath = prefix + "/" + filepath + suffix;
            Logger.debug("dataserver::ClientCat():: filepath:" + filepath);

            ByteBuffer fileData = FileHelper.randomReadFromFile(filepath, 0, 16);
            printByteBufferAsHex(fileData);
            fileData.flip();
            DfsProto responePacket = new DfsProto("cat", 16,"dummy", (short)0, fileData);

            return responePacket.encode();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private ByteBuffer handleClientPut(DfsProto packet) {
        try {
            ByteBuffer fileData = packet.data();
            short fileIdx = packet.fileIdx();
            String[] args = packet.args().split(" ");
            String storePath = args[0];
            String filename = args[1] + ".split" + String.valueOf(fileIdx);
            int offset;

            filename = genAbsolutePath(storePath, filename);

            if (fileRecorder.containsKey(filename)) {
                offset = fileRecorder.get(filename);
            } else {
                fileRecorder.put(filename, 0);
                offset = 0;
            }

            Logger.debug("dataServer::onReceive() filename: " + filename);
            FileHelper.randomWriteToFile(filename, offset, fileData.array());
            // TODO 16
            fileRecorder.put(filename, offset + 16);
            ByteBuffer nullResult = ByteBuffer.allocate(0);
            return nullResult;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ByteBuffer onReceive(ByteBuffer data) {
        Logger.debug("dataServer::onReceive()");

        ByteBuffer nullResult = ByteBuffer.allocate(0);
        DfsProto packet = DfsProto.decode(data);

        if (packet.command() == "put") {
            Logger.debug("dataserver::handleClientPut()");
            return handleClientPut(packet);
        }
        if (packet.command() == "cat") {
            Logger.debug("dataserver::handleClientCat()");
            return handleClientCat(packet);
        }
        return nullResult;
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
