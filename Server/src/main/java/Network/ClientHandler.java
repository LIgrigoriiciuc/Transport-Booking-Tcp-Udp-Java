package Network;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private final NetworkServiceImpl service;
    private static final Gson gson = new Gson();
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;
    private Long loggedUserId = null;
    public ClientHandler(NetworkServiceImpl service, Socket socket) {
        this.service = service;
        this.socket  = socket;
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())));
        } catch (IOException ignored) {}
    }
    @Override
    public void run() {
        while (running) {
            try {
                String line = in.readLine();
                if (line == null) { running = false; break; }
                System.out.println("[ClientHandler] ← " + line);

                Packet request  = gson.fromJson(line, Packet.class);
                Packet response = dispatch(request);
                send(response);

            } catch (IOException e) {
                System.out.println("[ClientHandler] Disconnected: " + socket.getInetAddress());
                running = false;
            }
        }
        if (loggedUserId != null)
            service.forceLogout(loggedUserId);
        close();
    }
    private void send(Packet response) {
        String line = gson.toJson(response);
        synchronized (out) {
            out.println(line);
            out.flush();
        }
    }

    private void close() {
        try { in.close(); out.close(); socket.close(); }
        catch (IOException ignored) { }
    }
}
