package Network;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpConnection {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private static final Gson gson = new Gson();

    public TcpConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);
        writer = new PrintWriter(socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void send(Packet p) {
        String json = gson.toJson(p);
        writer.println(json);
        writer.flush();
    }
    public String readLine() throws IOException {
        return reader.readLine(); }
    public void close() throws IOException {
        socket.close(); }
}