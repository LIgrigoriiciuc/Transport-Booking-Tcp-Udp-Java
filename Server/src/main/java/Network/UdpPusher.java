package Network;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class UdpPusher implements AutoCloseable {

    private static final Logger logger = LogManager.getLogger(UdpPusher.class);
    private final DatagramSocket socket;
    public UdpPusher() throws SocketException {
        socket = new DatagramSocket(); //ephemeral port
    }
    public void push(InetSocketAddress addr) {
        try {
            logger.debug("Sending UDP push to {}", addr);
            byte[] data = "PUSH".getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(data, data.length, addr.getAddress(), addr.getPort());
            socket.send(packet);
        } catch (IOException e) {
            logger.error("UDP push failed to {}", addr, e);
        }
    }
    public void close() { socket.close(); }
}
