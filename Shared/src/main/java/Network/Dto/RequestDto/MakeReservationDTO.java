package Network.Dto.RequestDto;

public class MakeReservationDTO {
    private String clientName;
    private java.util.List<Long> seatIds;
    private Long userId;
    public MakeReservationDTO() { }
    public MakeReservationDTO(String clientName, java.util.List<Long> seatIds, Long userId) {
        this.clientName = clientName;
        this.seatIds = seatIds;
        this.userId = userId;
    }
    public String getClientName() {
        return clientName;
    }
    public java.util.List<Long> getSeatIds() {
        return seatIds;
    }
    public Long getUserId() {
        return userId;
    }
}
