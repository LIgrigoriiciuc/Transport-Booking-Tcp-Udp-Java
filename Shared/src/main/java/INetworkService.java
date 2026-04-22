import Network.Dto.RequestDto.*;
import Network.Dto.ResponseDto.ReservationDTO;
import Network.Dto.ResponseDto.SeatDTO;
import Network.Dto.ResponseDto.TripDTO;
import Network.Dto.ResponseDto.UserDTO;

import java.util.List;

public interface INetworkService {

    UserDTO login(LoginDTO dto);
    void logout(LogoutDTO dto);

    List<TripDTO> searchTrips(SearchTripsDTO dto);

    List<SeatDTO> getSeatsForTrip(GetSeatsDTO dto);

    void makeReservation(MakeReservationDTO dto);

    void cancelReservation(CancelReservationDTO dto);

    List<ReservationDTO> getAllReservations();
}