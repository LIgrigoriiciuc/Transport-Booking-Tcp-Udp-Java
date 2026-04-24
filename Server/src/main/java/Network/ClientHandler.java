package Network;

import Domain.User;
import Network.Dto.RequestDto.ConnectDTO;
import Network.Dto.RequestDto.LoginDTO;
import Network.Dto.RequestDto.LogoutDTO;
import Network.Dto.RequestDto.SearchTripsDTO;
import Network.Dto.ResponseDto.ReservationDTO;
import Network.Dto.ResponseDto.SeatDTO;
import Network.Dto.ResponseDto.TripDTO;
import Network.Dto.ResponseDto.UserDTO;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final NetworkServiceImpl service;
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;
    private Long loggedUserId;
    private String connectionId = null; // assigned at CONNECT

    private static final Gson gson = new Gson();

    public ClientHandler(NetworkServiceImpl service, Socket socket) {
        this.service = service;
        this.socket  = socket;
        try {
            out = new PrintWriter(socket.getOutputStream());
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                String line = in.readLine();
                if (line == null) { running = false; break; }
                Packet request  = gson.fromJson(line, Packet.class);
                Packet response = handleRequest(request);
                if (response != null) sendToClient(response);
            } catch (IOException e) {
                running = false;
            }
        }
        // TCP dropped — clean up everything for this connection
        service.forceLogout(connectionId);
        close();
    }

    private Packet handleRequest(Packet req) {
        try {
            switch (req.getAction()) {

                case CONNECT: {
                    ConnectDTO dto = req.getConnectData();
                    connectionId = service.registerConnection(
                            socket.getInetAddress(), dto.getUdpPort());
                    NetworkServiceImpl.setConnectionId(connectionId); // ← set for this thread
                    return PacketFactory.ok();
                }

                case LOGIN: {
                    LoginDTO dto = req.getLoginData();
                    UserDTO user = service.login(dto);
                    loggedUserId = user.getId();
                    return PacketFactory.loginOk(user);
                }

                case LOGOUT: {
                    service.logout(req.getLogoutData());
                    loggedUserId = null;
                    // running stays true — connection alive, back to login
                    return PacketFactory.ok();
                }

                case SEARCH_TRIPS: {
                    List<TripDTO> trips = service.searchTrips(req.getSearchTripsData());
                    return PacketFactory.trips(trips);
                }

                case GET_SEATS: {
                    List<SeatDTO> seats = service.getSeatsForTrip(req.getGetSeatsData());
                    return PacketFactory.seats(seats);
                }

                case MAKE_RESERVATION: {
                    service.makeReservation(req.getMakeReservationData());
                    return PacketFactory.ok();
                }

                case CANCEL_RESERVATION: {
                    service.cancelReservation(req.getCancelReservationData());
                    return PacketFactory.ok();
                }

                case GET_RESERVATIONS: {
                    List<ReservationDTO> res = service.getAllReservations();
                    return PacketFactory.reservations(res);
                }

                default:
                    return PacketFactory.error("Unknown action: " + req.getAction());
            }
        } catch (Exception e) {
            return PacketFactory.error(e.getMessage());
        }
    }

    private void sendToClient(Packet response) {
        String line = gson.toJson(response);
        synchronized (out) {
            out.println(line);
            out.flush();
        }
    }

    private void close() {
        try { in.close(); out.close(); socket.close(); }
        catch (IOException ignored) {}
    }
}