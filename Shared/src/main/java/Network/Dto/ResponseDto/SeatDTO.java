package Network.Dto.ResponseDto;

public class SeatDTO {
    private Long id;
    private int number;
    private boolean reserved;
    private Long tripId;
    private Long reservationId;
    public SeatDTO() { }
    public SeatDTO(Long id, int number, boolean reserved, Long tripId, Long reservationId) {
        this.id = id;
        this.number = number;
        this.reserved = reserved;
        this.tripId = tripId;
        this.reservationId = reservationId;
    }
    public Long getId() { return id; }
    public int getNumber() { return number; }
    public boolean isReserved() { return reserved; }
    public Long getTripId() { return tripId; }
    public Long getReservationId() { return reservationId; }
}
