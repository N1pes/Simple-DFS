import java.io.IOException;
import java.net.*;
import java.io.*;

public class Connector {
    private int             port;
    private String          ip;
    Socket                  clientSocket;
    private PrintWriter     out; // TODO
    private BufferedReader  in;

    Connector(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void connect() {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Logger.success("Successfully connect to " + ip + ":" + String.valueOf(port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(String msg) {
        out.println(msg);
    }

    public String recv() {
        String resp;
        try {
            resp = in.readLine();
            return resp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
