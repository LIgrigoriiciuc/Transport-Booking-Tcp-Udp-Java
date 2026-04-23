package Network;

import Network.Dto.RequestDto.*;
import Network.Dto.ResponseDto.ReservationDTO;
import Network.Dto.ResponseDto.SeatDTO;
import Network.Dto.ResponseDto.TripDTO;
import Network.Dto.ResponseDto.UserDTO;

import java.util.List;

public class Packet {
    private Action action;
    private String error;
    private LoginDTO loginData;
    private LogoutDTO logoutData;
    private SearchTripsDTO searchTripsData;
    private GetSeatsDTO getSeatsData;
    private MakeReservationDTO makeReservationData;
    private CancelReservationDTO cancelReservationData;
    private UserDTO user;
    private List<TripDTO> trips;
    private List<SeatDTO> seats;
    private List<ReservationDTO> reservations;

    public Packet() {
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public LoginDTO getLoginData() {
        return loginData;
    }

    public void setLoginData(LoginDTO loginData) {
        this.loginData = loginData;
    }

    public LogoutDTO getLogoutData() {
        return logoutData;
    }

    public void setLogoutData(LogoutDTO logoutData) {
        this.logoutData = logoutData;
    }

    public SearchTripsDTO getSearchTripsData() {
        return searchTripsData;
    }

    public void setSearchTripsData(SearchTripsDTO searchTripsData) {
        this.searchTripsData = searchTripsData;
    }

    public GetSeatsDTO getGetSeatsData() {
        return getSeatsData;
    }

    public void setGetSeatsData(GetSeatsDTO getSeatsData) {
        this.getSeatsData = getSeatsData;
    }

    public MakeReservationDTO getMakeReservationData() {
        return makeReservationData;
    }

    public void setMakeReservationData(MakeReservationDTO makeReservationData) {
        this.makeReservationData = makeReservationData;
    }

    public CancelReservationDTO getCancelReservationData() {
        return cancelReservationData;
    }

    public void setCancelReservationData(CancelReservationDTO cancelReservationData) {
        this.cancelReservationData = cancelReservationData;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public List<TripDTO> getTrips() {
        return trips;
    }

    public void setTrips(List<TripDTO> trips) {
        this.trips = trips;
    }

    public List<SeatDTO> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatDTO> seats) {
        this.seats = seats;
    }

    public List<ReservationDTO> getReservations() {
        return reservations;
    }

    public void setReservations(List<ReservationDTO> reservations) {
        this.reservations = reservations;
    }
}