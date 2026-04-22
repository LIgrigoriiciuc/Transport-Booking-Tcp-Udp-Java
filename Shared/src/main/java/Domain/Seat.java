package Domain;

public class Seat extends Entity<Long> {
    private Integer number;
    private boolean isReserved;
    private Long tripId;
    private Long reservationId;
    private Trip trip;
    private Reservation reservation;

    public Seat(Long id, Integer number, boolean isReserved, Long tripId, Long reservationId) {
        super(id);
        this.number = number;
        this.isReserved = isReserved;
        this.tripId = tripId;
        this.reservationId = reservationId;
    }

    public Integer getNumber() { return number; }
    public boolean isReserved() { return isReserved; }
    public Long getTripId() { return tripId; }
    public Long getReservationId() { return reservationId; }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }
}