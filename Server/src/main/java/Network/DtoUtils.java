package Network;

import Domain.*;
import Network.Dto.ResponseDto.ReservationDTO;
import Network.Dto.ResponseDto.SeatDTO;
import Network.Dto.ResponseDto.TripDTO;
import Network.Dto.ResponseDto.UserDTO;
import Util.DateTimeUtils;
import java.util.ArrayList;
import java.util.List;

public class DtoUtils {
    public static TripDTO toDto(Trip trip, Integer freeSeats) {
        return new TripDTO(
                trip.getId(),
                trip.getDestination(),
                DateTimeUtils.format(trip.getTime()),
                trip.getBusNumber(),
                freeSeats
        );
    }
    public static List<TripDTO> tripsToDto(List<Trip> trips, List<Integer> freeSeatsPerTrip) {
        List<TripDTO> result = new ArrayList<>();
        for (int i = 0; i < trips.size(); i++) {
            result.add(toDto(trips.get(i), freeSeatsPerTrip.get(i)));
        }
        return result;
    }

    public static SeatDTO toDto(Seat seat) {
        return new SeatDTO(
                seat.getId(),
                seat.getNumber(),
                seat.isReserved(),
                seat.getTripId(),
                seat.getReservationId()
        );
    }
    public static List<SeatDTO> seatsToDto(List<Seat> seats) {
        return seats.stream()
                .map(DtoUtils::toDto)
                .toList();
    }
    public static UserDTO toDto(User user, Office office) {
        return new UserDTO(
                user.getId(),
                user.getFullName(),
                office != null ? office.getAddress() : null
        );
    }
    public static ReservationDTO toDto(Reservation reservation, List<Integer> seats, User user, Long tripId){
        return new ReservationDTO(
                reservation.getId(),
                reservation.getClientName(),
                DateTimeUtils.format(reservation.getReservationTime()),
                tripId,
                seats,
                user.getUsername()
        );
    }

    public static List<ReservationDTO> reservationsToDto(
            List<Reservation> reservations,
            List<List<Integer>> seatsPerReservation,
            List<User> users,
            List<Long> tripIds) {
        List<ReservationDTO> result = new ArrayList<>();
        for (int i = 0; i < reservations.size(); i++) {
            result.add(toDto(reservations.get(i), seatsPerReservation.get(i), users.get(i), tripIds.get(i)));
        }
        return result;
    }



}
