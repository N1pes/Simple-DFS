import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

class ServerConfig {
    private String serverName;
    private String ip;
    private int port;

    public ServerConfig(String serverName, String ip, int port) {
        this.serverName = serverName;
        this.ip = ip;
        this.port = port;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    @Override
    public String toString() {
        return "ServerConfig{" +
                "serverName='" + serverName + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}

public class JsonParser {
    public static List<ServerConfig> parseServerConfig(String filePath) {
        List<ServerConfig> serverConfigs = new ArrayList<>();

        try (FileReader reader = new FileReader(filePath)) {
            StringBuilder jsonText = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                jsonText.append((char) c);
            }
            JSONArray jsonArray = new JSONArray(jsonText.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject serverObject = jsonArray.getJSONObject(i);
                String serverName = serverObject.getString("ServerName");
                String ip = serverObject.getString("ip");
                int port = serverObject.getInt("port");

                ServerConfig serverConfig = new ServerConfig(serverName, ip, port);
                serverConfigs.add(serverConfig);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return serverConfigs;
    }
}