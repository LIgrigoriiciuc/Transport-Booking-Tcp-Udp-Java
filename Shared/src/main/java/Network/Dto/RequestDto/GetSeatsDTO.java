package Network.Dto.RequestDto;

public class GetSeatsDTO {
    private Long tripId;
    public GetSeatsDTO() { }
    public GetSeatsDTO(Long tripId) { this.tripId = tripId; }
    public Long getTripId() { return tripId; }
}
