import java.nio.channels.SocketChannel;

public class ClientInfo {

    public SocketChannel   clientSocket;
    public String          data;

    ClientInfo(SocketChannel clientSocket, String data) {
        this.clientSocket = clientSocket;
        this.data = data;
    }

}
