package Network;

import com.google.gson.Gson;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

public class TcpReaderThread implements Runnable {

    private static final Logger logger = LogManager.getLogger(TcpReaderThread.class);
    private final TcpConnection tcp;
    private final IResponseReceiver receiver;
    private final Gson gson = new Gson();
    private volatile boolean running = true;
    public TcpReaderThread(TcpConnection tcp, IResponseReceiver receiver) {
        this.tcp = tcp;
        this.receiver = receiver;
    }
    @Override
    public void run() {
        while (running) {
            try {
                String line = tcp.readLine();
                if (line == null) break;
                logger.debug("Received TCP packet: {}", line);
                Packet p = gson.fromJson(line, Packet.class);
                receiver.enqueueResponse(p);
            } catch (IOException e) {
                logger.error("IOException in TCP reader", e);
                if (running)
                    receiver.enqueueResponse(PacketFactory.error("Server disconnected"));
                break;
            }
        }
    }
    public void stop() { running = false; }
}
