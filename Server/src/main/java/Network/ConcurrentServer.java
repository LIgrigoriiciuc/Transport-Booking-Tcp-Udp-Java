package Network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConcurrentServer {
    private final int port;
    private final NetworkServiceImpl service;
    private ServerSocket serverSocket;

    public ConcurrentServer(int port, NetworkServiceImpl service) {
        this.port = port;
        this.service = service;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        try {
            while (true) {
                Socket client = serverSocket.accept();
                Thread t = new Thread(new ClientHandler(service, client));
                t.setDaemon(true);
                t.start();
            }
        } finally {
            serverSocket.close();
        }
    }
    public void stop() throws IOException {
        if (serverSocket != null) serverSocket.close();
    }
}
