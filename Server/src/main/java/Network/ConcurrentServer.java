package Network;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConcurrentServer {

    private static final Logger logger = LogManager.getLogger(ConcurrentServer.class);
    private final int port;
    private final NetworkServiceImpl service;
    private ServerSocket serverSocket;

    public ConcurrentServer(int port, NetworkServiceImpl service) {
        this.port = port;
        this.service = service;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        logger.info("Server started on port {}", port);
        try {
            while (true) {
                Socket client = serverSocket.accept();
                logger.info("Accepted client connection from {}", client.getInetAddress());
                //~1KB instead of the 1MB Thread cost
                Thread.ofVirtual().start(new ClientHandler(service, client));
            }
        } finally {
            logger.info("Server stopped");
            serverSocket.close();
        }
    }
    public void stop() throws IOException {
        if (serverSocket != null) serverSocket.close();
    }
}
