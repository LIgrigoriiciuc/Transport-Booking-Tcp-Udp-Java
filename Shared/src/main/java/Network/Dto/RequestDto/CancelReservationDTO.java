package Network.Dto.RequestDto;

public class CancelReservationDTO {
    private Long reservationId;
    public CancelReservationDTO() { }
    public CancelReservationDTO(Long reservationId) {
        this.reservationId = reservationId;
    }
    public Long getReservationId() {
        return reservationId;
    }
}
