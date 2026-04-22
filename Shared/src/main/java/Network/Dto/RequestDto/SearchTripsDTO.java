package Network.Dto.RequestDto;

public class SearchTripsDTO {
    private String destination;
    private String from;
    private String to;
    public SearchTripsDTO() { }
    public SearchTripsDTO(String destination, String from, String to) {
        this.destination = destination;
        this.from = from;
        this.to = to;
    }
    public String getDestination() {
        return destination;
    }
    public String getFrom() {
        return from;
    }
    public String getTo() {
        return to;
    }
}
