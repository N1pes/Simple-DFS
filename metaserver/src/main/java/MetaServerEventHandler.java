import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class MetaServerEventHandler implements IOCallback {

    static private class DataServerInfo {
        int             port;
        long            diskUsage;
        String          ip;
    }

    private final       List<String> tmpDataServers = new ArrayList<>(Arrays.asList(
            "127.0.0.1:9996",
            "127.0.0.1:9995"
    ));

    private int                 numDataServers;
    List<MetaServerEventHandler.DataServerInfo> dataServers;

    // handle master
    @Override
    public ByteBuffer onReceive(ByteBuffer data) {
        Logger.log("MetaServer onReceive(): " );
        DfsProto packet = DfsProto.decode(data);
        ByteBuffer result;
        int fileSize;

        if (!packet.command().equals("get") && !packet.args().equals("ip")) {
            Logger.error("MetaServer: received invalid command: " +
                    packet.command() + " " + packet.args());

            result = ByteBuffer.allocate("Bad IP Request".length());
            result.put(ByteBuffer.wrap("Bad IP Request".getBytes()));
            return result;
        }
        fileSize = packet.fileSize();
        Logger.debug("MetaServer onReceive(): " + String.valueOf(fileSize));

        result = handleIPAllocation(fileSize);
        Logger.log(result.toString());
        return result;
    }

    // test for now
    public ByteBuffer handleIPAllocation(int fileSize) {
        int nBlock = fileSize / 16;
        int nServer = 2;
        int addrSize = 0;

        ByteBuffer result;
        if (fileSize % 16 != 0)
            nBlock += 1;

        for (int i = 0; i < nBlock; i++) {
            String dataServerAddr = tmpDataServers.get(i % nServer);
            addrSize += dataServerAddr.length() + 1;
        }
        result = ByteBuffer.allocate(addrSize - 1);
        for (int i = 0; i < nBlock; i++) {
            String dataServerAddr = tmpDataServers.get(i % nServer);
            if (i != nBlock -1)
                dataServerAddr += " ";
            result.put(ByteBuffer.wrap(dataServerAddr.getBytes()));
        }

        return result;
    }


}
