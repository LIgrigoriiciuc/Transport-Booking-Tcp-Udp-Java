package Domain;

import java.time.LocalDateTime;

public class Reservation extends Entity<Long> {
    private String clientName;
    private LocalDateTime reservationTime;
    private Long userId;
    private User user;

    public Reservation(String clientName, Long userId) {
        super();
        this.clientName = clientName;
        this.reservationTime = LocalDateTime.now();
        this.userId = userId;
    }

    public Reservation(Long id, String clientName, LocalDateTime reservationTime) {
        super(id);
        this.clientName = clientName;
        this.reservationTime = reservationTime;
    }

    public String getClientName() { return clientName; }
    public LocalDateTime getReservationTime() { return reservationTime; }

    public void setUser(User user) {
        this.user = user;
    }
}