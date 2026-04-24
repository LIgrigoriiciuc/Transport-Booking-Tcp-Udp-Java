package Network;

import Network.Dto.RequestDto.*;
import Network.Dto.ResponseDto.ReservationDTO;
import Network.Dto.ResponseDto.SeatDTO;
import Network.Dto.ResponseDto.TripDTO;
import Network.Dto.ResponseDto.UserDTO;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkProxy implements INetworkService, IResponseReceiver, IPushReceiver {
    private final String host;
    private final int port;
    private TcpConnection tcp;
    private TcpReaderThread readerThread;
    private UdpListenerThread udpListener;
    private Runnable onPush;
    private final BlockingQueue<Packet> responses = new LinkedBlockingQueue<>();

    public NetworkProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public void setOnPush(Runnable r) { this.onPush = r; }

    @Override
    public void enqueueResponse(Packet p) {
        try {
            responses.put(p);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    @Override
    public void onPushReceived() {
        if (onPush != null) onPush.run();
    }

    private Packet sendAndReceive(Packet request) {
        tcp.send(request);
        try {
            return responses.take(); // blocks until ReaderThread puts something in
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted waiting for response");
        }
    }
    public void connect() throws IOException {
        tcp = new TcpConnection(host, port);
        udpListener = new UdpListenerThread(0, this);
        Thread udpThread = new Thread(udpListener, "udp-listener");
        udpThread.setDaemon(true);
        udpThread.start();
        readerThread = new TcpReaderThread(tcp, this);
        Thread tcpThread = new Thread(readerThread, "tcp-reader");
        tcpThread.setDaemon(true);
        tcpThread.start();
    }

    @Override
    public UserDTO login(LoginDTO dto) {
        LoginDTO dtoWithPort = new LoginDTO(dto.getUsername(), dto.getPassword(), udpListener.getPort());
        return exchange(PacketFactory.login(dtoWithPort)).getUser();
    }

    @Override
    public void logout(LogoutDTO dto) {
        exchange(PacketFactory.logout(dto));
    }

    @Override
    public List<TripDTO> searchTrips(SearchTripsDTO dto) {
        return exchange(PacketFactory.searchTrips(dto)).getTrips();
    }

    @Override
    public List<SeatDTO> getSeatsForTrip(GetSeatsDTO dto) {
        return exchange(PacketFactory.getSeats(dto)).getSeats();
    }

    @Override
    public void makeReservation(MakeReservationDTO dto) {
        exchange(PacketFactory.makeReservation(dto));
    }

    @Override
    public void cancelReservation(CancelReservationDTO dto) {
        exchange(PacketFactory.cancelReservation(dto));
    }

    @Override
    public List<ReservationDTO> getAllReservations() {
        return exchange(PacketFactory.getReservations()).getReservations();
    }

    private Packet exchange(Packet request) {
        Packet response = sendAndReceive(request);
        if (response.getAction() == Action.ERROR)
            throw new RuntimeException(response.getError());
        return response;
    }

    public void disconnect() throws IOException {
        if (readerThread != null) readerThread.stop();
        if (udpListener != null) udpListener.stop();
        if (tcp != null) tcp.close();
    }
}