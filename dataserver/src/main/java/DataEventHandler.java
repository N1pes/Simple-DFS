import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.io.File;

// handle data server on receive event
public class DataEventHandler implements IOCallback {

    private Map<String, Integer>        fileRecorder = new HashMap<>();
    private String                      dataRootPath = "/DFSData";

    private String genAbsolutePath(String path) {
        return dataRootPath + File.separator + path;
    }

    @Override
    public ByteBuffer onReceive(ByteBuffer data) {
        Logger.debug("dataServer::onReceive()");
        try {
            ByteBuffer nullResult = ByteBuffer.allocate(0);
            DfsProto packet = DfsProto.decode(data);
            ByteBuffer fileData = packet.data();
            short fileIdx = packet.fileIdx();
            String filename = packet.args() + ".split" + String.valueOf(fileIdx);
            int offset;

            filename = genAbsolutePath(filename);

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
            return nullResult;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
