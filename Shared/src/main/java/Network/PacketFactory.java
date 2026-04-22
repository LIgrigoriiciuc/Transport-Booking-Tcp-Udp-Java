package Network;

import Network.Dto.RequestDto.*;
import Network.Dto.ResponseDto.ReservationDTO;
import Network.Dto.ResponseDto.SeatDTO;
import Network.Dto.ResponseDto.TripDTO;
import Network.Dto.ResponseDto.UserDTO;

import java.util.List;

public class PacketFactory {
    public static Packet login(LoginDTO dto) {
        Packet p = new Packet();
        p.setAction(Action.LOGIN);
        p.setPayload(dto);
        return p;
    }
    public static Packet logout(LogoutDTO dto) {
        Packet p = new Packet();
        p.setAction(Action.LOGOUT);
        p.setPayload(dto);
        return p;
    }
    public static Packet searchTrips(SearchTripsDTO dto) {
        Packet p = new Packet();
        p.setAction(Action.SEARCH_TRIPS);
        p.setPayload(dto);
        return p;
    }
    public static Packet getSeats(GetSeatsDTO dto) {
        Packet p = new Packet();
        p.setAction(Action.GET_SEATS);
        p.setPayload(dto);
        return p;
    }
    public static Packet getReservations() {
        Packet p = new Packet();
        p.setAction(Action.GET_RESERVATIONS);
        return p;
    }
    public static Packet makeReservation(MakeReservationDTO dto) {
        Packet p = new Packet();
        p.setAction(Action.MAKE_RESERVATION);
        p.setPayload(dto);
        return p;
    }
    public static Packet cancelReservation(CancelReservationDTO dto) {
        Packet p = new Packet();
        p.setAction(Action.CANCEL_RESERVATION);
        p.setPayload(dto);
        return p;
    }
    public static Packet ok() {
        Packet p = new Packet();
        p.setAction(Action.OK);
        return p;
    }

    public static Packet error(String message) {
        Packet p = new Packet();
        p.setAction(Action.ERROR);
        p.setError(message);
        return p;
    }
    public static Packet loginOk(UserDTO dto) {
        Packet p = new Packet();
        p.setAction(Action.LOGIN);
        p.setPayload(dto);
        return p;
    }
    public static Packet trips(List<TripDTO> trips) {
        Packet p = new Packet();
        p.setAction(Action.SEARCH_TRIPS);
        p.setPayload(trips);
        return p;
    }
    public static Packet seats(List<SeatDTO> seats) {
        Packet p = new Packet();
        p.setAction(Action.GET_SEATS);
        p.setPayload(seats);
        return p;
    }

    public static Packet reservations(List<ReservationDTO> reservations) {
        Packet p = new Packet();
        p.setAction(Action.GET_RESERVATIONS);
        p.setPayload(reservations);
        return p;
    }

    public static Packet reservationOk(ReservationDTO dto) {
        Packet p = new Packet();
        p.setAction(Action.MAKE_RESERVATION);
        p.setPayload(dto);
        return p;
    }
    public static Packet push() {
        Packet p = new Packet();
        p.setAction(Action.PUSH_UPDATE);
        return p;
    }
}
