package Network.Dto.ResponseDto;

import java.util.List;

public class ReservationDTO {
    private Long id;
    private String clientName;
    private String reservationTime;
    private Long tripId;
    private List<Integer> seatNumbers;
    private String userUsername;
    public ReservationDTO() { }
    public ReservationDTO(Long id, String clientName, String reservationTime, Long tripId, List<Integer> seatNumbers, String userUsername) {
        this.id = id;
        this.clientName = clientName;
        this.reservationTime = reservationTime;
        this.tripId = tripId;
        this.seatNumbers = seatNumbers;
        this.userUsername = userUsername;
    }
    public Long getId() {
        return id;
    }
    public String getClientName() {
        return clientName;
    }
    public String getReservationTime() {
        return reservationTime;
    }
    public Long getTripId() {
        return tripId;
    }
    public List<Integer> getSeatNumbers() {
        return seatNumbers;
    }
    public String getUserUsername() {
        return userUsername;
    }
}
