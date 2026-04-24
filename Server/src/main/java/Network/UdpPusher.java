package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class UdpPusher {
    private DatagramSocket socket;
    public UdpPusher() throws SocketException {
        socket = new DatagramSocket(); // ephemeral port, just for sending
    }
    public void notifyClients(Collection<InetSocketAddress> clients, String notifType) {
        for (InetSocketAddress addr : clients) {
            notifyClient(addr, notifType);
        }
    }
    public void notifyClient(InetSocketAddress clientAddr, String notifType) {
        try {
            byte[] data = notifType.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    clientAddr.getAddress(),
                    clientAddr.getPort()
            );
            socket.send(packet);
            } catch (IOException e) {
        }
    }
    public void close() { socket.close(); }
}
