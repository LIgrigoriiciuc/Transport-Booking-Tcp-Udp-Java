package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class UdpPusher implements AutoCloseable {
    private final DatagramSocket socket;
    public UdpPusher() throws SocketException {
        socket = new DatagramSocket(); //ephemeral port
    }
    public void push(InetSocketAddress addr) {
        try {
            byte[] data = "PUSH".getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(data, data.length, addr.getAddress(), addr.getPort());
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("UDP push failed to " + addr + ": " + e.getMessage());
        }
    }
    public void close() { socket.close(); }
}
