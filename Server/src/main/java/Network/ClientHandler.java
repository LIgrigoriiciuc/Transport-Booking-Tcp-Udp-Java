package Network;
import Network.Dto.RequestDto.ConnectDTO;
import Network.Dto.RequestDto.LoginDTO;
import Network.Dto.ResponseDto.ReservationDTO;
import Network.Dto.ResponseDto.SeatDTO;
import Network.Dto.ResponseDto.TripDTO;
import Network.Dto.ResponseDto.UserDTO;
import com.google.gson.Gson;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(ClientHandler.class);
    private final NetworkServiceImpl service;
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;
    private Long loggedUserId;
    private String connectionId = null; //assigned at CONNECT
    private static final Gson gson = new Gson();

    public ClientHandler(NetworkServiceImpl service, Socket socket) throws IOException {
        this.service = service;
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        while (running) { //in.readLine() returns null/IOE/
            try {
                String line = in.readLine();
                if (line == null) { running = false; break; }
                logger.debug("Received request: {}", line);
                Packet request  = gson.fromJson(line, Packet.class);
                Packet response = handleRequest(request);
                if (response != null) sendToClient(response);
            } catch (IOException e) {
                logger.error("IOException in client handler", e);
                running = false;
            }
        }
        //TCP dropped
        service.forceLogout(connectionId);
        close();
    }

    private Packet handleRequest(Packet req) {
        try {
            switch (req.getAction()) {

                case CONNECT: {
                    ConnectDTO dto = req.getConnectData();
                    connectionId = service.registerConnection(socket.getInetAddress(), dto.getUdpPort());
                    NetworkServiceImpl.setConnectionId(connectionId);
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
            logger.error("Error handling request {}", req.getAction(), e);
            return PacketFactory.error(e.getMessage());
        }
    }

    private void sendToClient(Packet response) {
        logger.debug("Sending response: {}", response);
        String line = gson.toJson(response);
        out.println(line);
        out.flush();
    }

    private void close() {
        try {
            in.close();
            out.close();
            socket.close(); }
        catch (IOException ignored) {}
    }
}