package Network;

import Domain.*;
import Network.Dto.RequestDto.*;
import Network.Dto.ResponseDto.ReservationDTO;
import Network.Dto.ResponseDto.SeatDTO;
import Network.Dto.ResponseDto.TripDTO;
import Network.Dto.ResponseDto.UserDTO;
import Service.FacadeService;
import Util.DateTimeUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkServiceImpl implements INetworkService {

    private static final Logger logger = LogManager.getLogger(NetworkServiceImpl.class);

    private final FacadeService facade;
    private final UdpPusher udpPusher;
    //using 2 maps to avoid O(n) search
    private final Map<String, ConnectionSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, String> userIndex = new ConcurrentHashMap<>();
    private final Object loginLock = new Object();
    private final Object reservationLock = new Object();
    private final ExecutorService pushExecutor = Executors.newFixedThreadPool(4);
    private static final ThreadLocal<String> currentConnectionId = new ThreadLocal<>();
    public NetworkServiceImpl(FacadeService facade, UdpPusher udpPusher) {
        this.facade = facade;
        this.udpPusher = udpPusher;
    }
    public static void setConnectionId(String id) {
        currentConnectionId.set(id);
    }

    private String getConnectionId() {
        return currentConnectionId.get();
    }

    public String registerConnection(InetAddress ip, int udpPort) {
        String id = UUID.randomUUID().toString();
        sessions.put(id, new ConnectionSession(new InetSocketAddress(ip, udpPort)));
        logger.info("Registered connection from {}:{}", ip, udpPort);
        return id;
    }

    @Override
    public UserDTO login(LoginDTO dto) {
        String connectionId = getConnectionId();
        User user = facade.login(dto.getUsername(), dto.getPassword());
        Office office = facade.getOfficeById(user.getOfficeId());
        synchronized (loginLock) {
            if (userIndex.putIfAbsent(user.getId(), connectionId) != null) {
                logger.warn("User {} already logged in", user.getId());
                throw new RuntimeException("User already logged in.");
            }
            sessions.get(connectionId).setUserId(user.getId());
        }
        logger.info("User {} logged in", user.getUsername());
        return DtoUtils.toDto(user, office);
    }

    @Override
    public void logout(LogoutDTO dto) {
        synchronized (loginLock) {
            String connectionId = userIndex.remove(dto.getUserId());
            if (connectionId != null)
                sessions.get(connectionId).setUserId(null);
        }
        logger.info("User {} logged out", dto.getUserId());
    }

    public void forceLogout(String connectionId) {
        //TCP dropped
        if (connectionId == null) return; //bye without connecting
        synchronized (loginLock) {
            ConnectionSession session = sessions.remove(connectionId);
            if (session != null && session.getUserId() != null)
                userIndex.remove(session.getUserId());
        }
        logger.info("Forced logout for connection {}", connectionId);
    }

    @Override
    public List<TripDTO> searchTrips(SearchTripsDTO dto) {
        LocalDateTime from = dto.getFrom() == null || dto.getFrom().isBlank() ? null : DateTimeUtils.parse(dto.getFrom());
        LocalDateTime to = dto.getTo() == null || dto.getTo().isBlank() ? null : DateTimeUtils.parse(dto.getTo());
        List<Trip> trips = facade.searchTrips(dto.getDestination(), from, to);
        List<Integer> freeSeats = trips.stream().map(t -> facade.countFreeSeats(t.getId())).toList();
        return DtoUtils.tripsToDto(trips, freeSeats);
    }

    @Override
    public List<SeatDTO> getSeatsForTrip(GetSeatsDTO dto) {
        return DtoUtils.seatsToDto(facade.getSeatsForTrip(dto.getTripId()));
    }

    @Override
    public void makeReservation(MakeReservationDTO dto) {
        synchronized (reservationLock) {
            List<Seat> seats = dto.getSeatIds().stream().map(facade::getSeatById).toList();
            for (Seat s : seats)
                if (s.isReserved()) {
                    logger.warn("Seat {} already reserved", s.getNumber());
                    throw new RuntimeException("Seat " + s.getNumber() + " is already reserved.");
                }
            facade.makeReservationForSeats(dto.getClientName(), seats, dto.getUserId());
        }
        logger.info("Reservation made by user {}", dto.getUserId());
        notifyPush();
    }

    @Override
    public void cancelReservation(CancelReservationDTO dto) {
        synchronized (reservationLock) {
            if (facade.getReservationById(dto.getReservationId()) == null) {
                logger.warn("Reservation {} not found", dto.getReservationId());
                throw new RuntimeException("Reservation not found or already cancelled.");
            }
            facade.cancelReservation(dto.getReservationId());
        }
        logger.info("Reservation {} cancelled", dto.getReservationId());
        notifyPush();
    }

    @Override
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = facade.getAllReservations();
        List<List<Integer>> seatsPerReservation = reservations.stream().map(r -> facade.getSeatNumbersByReservation(r.getId())).toList();
        List<User> users = reservations.stream().map(r -> facade.getUserById(r.getUserId())).toList();
        List<Long> tripIds = reservations.stream().map(r -> facade.getTripIdByReservation(r.getId())).toList();
        return DtoUtils.reservationsToDto(reservations, seatsPerReservation, users, tripIds);
    }

    private void notifyPush() {
        sessions.values().forEach(session ->
                pushExecutor.submit(() -> udpPusher.push(session.getUdpAddress()))
        );
    }
}