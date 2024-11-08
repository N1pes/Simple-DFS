import java.net.*;
import java.io.*;

public class Server extends Thread {

    private ServerSocket    serverSocket;
    private Socket          clientSocket; // TODO
    private Thread          listenThread;
    private int             port;
    private PrintWriter     out;
    private BufferedReader  in;


    @Override
    public void run() {
        startServer();
    }

    private void startServer() {
        try {
            Logger.log("Starting server...");
            serverSocket = new ServerSocket(this.port);
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Logger.log("client socket: " + String.valueOf(clientSocket));

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

    public void stopServer() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    Server(int port) {
        this.port = port;
    }
}
