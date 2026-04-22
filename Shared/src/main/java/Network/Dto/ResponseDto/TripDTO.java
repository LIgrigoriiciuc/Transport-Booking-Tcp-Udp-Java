package Network.Dto.ResponseDto;

public class TripDTO {
    private Long id;
    private String destination;
    private String time;
    private String busNumber;
    private Integer freeSeats;
    public TripDTO() { }
    public TripDTO(Long id, String destination, String time, String busNumber, Integer freeSeats) {
        this.id = id;
        this.destination = destination;
        this.time = time;
        this.busNumber = busNumber;
        this.freeSeats = freeSeats;
    }
    public Long getId() { return id; }
    public String getDestination() { return destination; }
    public String getTime() { return time; }
    public String getBusNumber() { return busNumber; }
    public Integer getFreeSeats() { return freeSeats; }
}
