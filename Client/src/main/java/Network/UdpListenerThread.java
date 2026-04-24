package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class UdpListenerThread implements Runnable {
    private final DatagramSocket socket;
    private final IPushReceiver receiver;
    private volatile boolean running = true;

    public UdpListenerThread(int port, IPushReceiver receiver) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.socket.setSoTimeout(2000);
        this.receiver = receiver;
    }

    public int getPort() { return socket.getLocalPort(); }

    @Override
    public void run() {
        byte[] buf = new byte[64];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                receiver.onPushReceived();
            } catch (SocketTimeoutException e) {
                // normal, loop again
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }
        socket.close();
    }

    public void stop() { running = false; }
}