package Network;

import Domain.User;
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
    private Long loggedUser;

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
                System.out.println("[ClientHandler] ← " + line);
                Packet request  = gson.fromJson(line, Packet.class);
                Packet response = handleRequest(request);
                if (response != null) sendToClient(response);  // only once
            } catch (IOException e) {
                running = false;
            }
        }
        if (loggedUser != null)
            service.forceLogout(loggedUser);
        close();
    }

    private Packet handleRequest(Packet req) {
        try {
            switch (req.getAction()) {

                case LOGIN: {
                    LoginDTO dto = req.getLoginData();
                    UserDTO user = service.login(dto, socket.getInetAddress());
                    return PacketFactory.loginOk(user);
                }

                case LOGOUT: {
                    LogoutDTO dto = req.getLogoutData();
                    service.logout(dto);
                    loggedUser = null;
                    running = false;
                    return PacketFactory.ok();
                }

                case SEARCH_TRIPS: {
                    SearchTripsDTO dto = req.getSearchTripsData();
                    String from = dto.getFrom(), to = dto.getTo();
                    List<TripDTO> trips = service.searchTrips(dto);
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
                    List<ReservationDTO> reservations = service.getAllReservations();
                    return PacketFactory.reservations(reservations);
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
        catch (IOException ignored) { }
    }
}