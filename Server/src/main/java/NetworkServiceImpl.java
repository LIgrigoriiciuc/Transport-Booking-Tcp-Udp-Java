import Domain.*;
import Network.Dto.RequestDto.*;
import Network.Dto.ResponseDto.ReservationDTO;
import Network.Dto.ResponseDto.SeatDTO;
import Network.Dto.ResponseDto.TripDTO;
import Network.Dto.ResponseDto.UserDTO;
import Network.DtoUtils;
import Service.FacadeService;
import Util.DateTimeUtils;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkServiceImpl implements INetworkService {
    private final FacadeService facade;
    private final Map<Long, InetAddress> loggedClients = new ConcurrentHashMap<>();
    private final InetAddress clientAddress;
    public NetworkServiceImpl(FacadeService facade,
                              InetAddress clientAddress) {
        this.facade = facade;
        this.clientAddress = clientAddress;
    }
//uuid
    @Override
    public UserDTO login(LoginDTO dto) {
        User user = facade.login(dto.getUsername(), dto.getPassword());
        if (loggedClients.containsKey(user.getId()))
            throw new RuntimeException("User already logged in.");
        loggedClients.put(user.getId(), clientAddress);
        Office office = facade.getOfficeById(user.getOfficeId());
        return DtoUtils.toDto(user, office);
    }
    @Override
    public void logout(LogoutDTO dto) {
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
//concurrency!!!!
    @Override
    public void makeReservation(MakeReservationDTO dto) {
        List<Seat> seats = dto.getSeatIds().stream()
                .map(facade::getSeatById)
                .toList();
        facade.makeReservationForSeats(dto.getClientName(), seats, dto.getUserId());
        notifyOthers();
}
    @Override
    public void cancelReservation(CancelReservationDTO dto) {
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

    private void notifyOthers() {
        loggedClients.entrySet().stream()
                .filter(e -> !e.getKey().equals(loggedInUser.getId()))
                .forEach(e -> udpNotifier.notify(e.getValue()));
    }
}
