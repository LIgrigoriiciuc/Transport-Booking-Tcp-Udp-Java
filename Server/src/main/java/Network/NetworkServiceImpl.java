package Network;

import Domain.*;
import Network.Dto.RequestDto.*;
import Network.Dto.ResponseDto.ReservationDTO;
import Network.Dto.ResponseDto.SeatDTO;
import Network.Dto.ResponseDto.TripDTO;
import Network.Dto.ResponseDto.UserDTO;
import Service.FacadeService;
import Util.DateTimeUtils;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkServiceImpl implements INetworkService {
    private final FacadeService facade;
    private final Map<Long, UdpTarget> loggedClients = new ConcurrentHashMap<>();
    private final UdpPusher udpPusher;
    private final ExecutorService pushExecutor = Executors.newFixedThreadPool(5);
    public NetworkServiceImpl(FacadeService facade, UdpPusher udpPusher) {
        this.facade = facade;
        this.udpPusher = udpPusher;
    }

    @Override
    public synchronized UserDTO login(LoginDTO dto, InetAddress clientAddress) {
        User user = facade.login(dto.getUsername(), dto.getPassword());
        if (loggedClients.containsKey(user.getId()))
            throw new RuntimeException("User already logged in.");
        loggedClients.put(user.getId(), clientAddress);
        //aici e ok
        Office office = facade.getOfficeById(user.getOfficeId());
        return DtoUtils.toDto(user, office);
    }
    @Override
    public synchronized void logout(LogoutDTO dto) {
        loggedClients.remove(dto.getUserId());
    }

    @Override
    public List<TripDTO> searchTrips(SearchTripsDTO dto) {
        LocalDateTime from = dto.getFrom() == null || dto.getFrom().isBlank()
                ? null : DateTimeUtils.parse(dto.getFrom());
        LocalDateTime to = dto.getTo() == null || dto.getTo().isBlank()
                ? null : DateTimeUtils.parse(dto.getTo());
        List<Trip> trips = facade.searchTrips(dto.getDestination(), from, to);
        List<Integer> freeSeats = trips.stream()
                .map(t -> facade.countFreeSeats(t.getId()))
                .toList();
        return DtoUtils.tripsToDto(trips, freeSeats);
    }

    @Override
    public List<SeatDTO> getSeatsForTrip(GetSeatsDTO dto) {
        return DtoUtils.seatsToDto(facade.getSeatsForTrip(dto.getTripId()));
    }

    @Override
    public synchronized void makeReservation(MakeReservationDTO dto) {
        List<Seat> seats = dto.getSeatIds().stream()
                .map(facade::getSeatById)
                .toList();
        for(Seat s: seats)
            if(s.isReserved())
                throw new RuntimeException("One of the selected seats is already reserved.");
        facade.makeReservationForSeats(dto.getClientName(), seats, dto.getUserId());
        notifyOthers();
}
    @Override
    public synchronized void cancelReservation(CancelReservationDTO dto) {
        if (facade.getReservationById(dto.getReservationId()) == null)
            throw new RuntimeException("Reservation already cancelled.");
        facade.cancelReservation(dto.getReservationId());
        notifyOthers();
    }
    @Override
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = facade.getAllReservations();
        List<List<Integer>> seatsPerReservation = reservations.stream()
                .map(r -> facade.getSeatNumbersByReservation(r.getId()))
                .toList();
        List<User> users = reservations.stream()
                .map(r -> facade.getUserById(r.getUserId()))
                .toList();

        List<Long> tripIds = reservations.stream()
                .map(r -> facade.getTripIdByReservation(r.getId()))
                .toList();
        return DtoUtils.reservationsToDto(reservations, seatsPerReservation, users, tripIds);
    }

    private void notifyOthers(Long excludeUserId) {
        Packet pushPacket = PacketFactory.push();
        loggedClients.entrySet().stream()
                .filter(e -> !e.getKey().equals(excludeUserId))
                .forEach(e -> pushExecutor.execute(
                        () -> udpPusher.push(e.getValue(), pushPacket)
                ));
    }
}
