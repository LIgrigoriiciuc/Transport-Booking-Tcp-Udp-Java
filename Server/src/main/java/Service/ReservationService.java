package Service;


import Domain.Reservation;
import Domain.Seat;
import Repository.ReservationRepository;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class ReservationService extends GenericService<Long, Reservation> {

    private static final Logger logger = LogManager.getLogger(ReservationService.class);

    private final SeatService seatService;

    public ReservationService(ReservationRepository reservationRepository,
                              SeatService seatService) {
        super(reservationRepository);
        this.seatService = seatService;
    }

    public void reserveSeats(String clientName, List<Seat> chosenSeats, Long userId) {
        if (clientName == null || clientName.isBlank()) {
            logger.warn("Client name cannot be empty");
            throw new IllegalArgumentException("Client name cannot be empty.");
        }
        if (chosenSeats.isEmpty()) {
            logger.warn("Must select at least one seat");
            throw new IllegalArgumentException("Must select at least one seat.");
        }
        logger.info("Reserving seats for client {} by user {}", clientName, userId);
        Reservation reservation = new Reservation(clientName, userId);
        repository.add(reservation);
        for (Seat seat : chosenSeats) {
            seat.setReserved(true);
            seat.setReservationId(reservation.getId());
            seatService.update(seat);
        }
    }

    public void cancel(long reservationId) {
        logger.info("Cancelling reservation {}", reservationId);
        List<Seat> seats = seatService.getByReservationId(reservationId);
        for (Seat seat : seats) {
            seat.setReserved(false);
            seat.setReservationId(null);
            seatService.update(seat);
        }
        repository.remove(reservationId);
    }


}