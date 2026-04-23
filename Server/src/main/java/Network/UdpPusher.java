package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpPusher implements AutoCloseable {
    private static final byte[] PAYLOAD = "REFRESH".getBytes(StandardCharsets.UTF_8);

    private final DatagramSocket socket;
    public UdpPusher() throws SocketException {
        this.socket = new DatagramSocket();
    }
    public void push(UdpTarget target, Packet packet) {
        try {
            byte[] data = gson.toJson(packet).getBytes(StandardCharsets.UTF_8);
            DatagramPacket dp = new DatagramPacket(
                    data, data.length,
                    target.address(), target.udpPort());
            socket.send(dp);
        } catch (IOException ignored) { }
    }
    @Override
    public void close() {
        socket.close();
    }
}
