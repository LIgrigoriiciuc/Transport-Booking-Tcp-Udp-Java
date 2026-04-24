package Network;

import com.google.gson.Gson;

import java.io.IOException;

public class TcpReaderThread implements Runnable {
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
                Packet p = gson.fromJson(line, Packet.class);
                receiver.enqueueResponse(p);
            } catch (IOException e) {
                if (running) e.printStackTrace();
                break;
            }
        }
    }
    public void stop() { running = false; }
}
