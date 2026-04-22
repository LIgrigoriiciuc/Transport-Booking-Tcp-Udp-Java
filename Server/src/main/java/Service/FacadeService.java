package Service;


import Domain.Reservation;
import Domain.Seat;
import Domain.Trip;
import Domain.User;
import Service.TransactionsLogic.TransactionManager;

import java.time.LocalDateTime;
import java.util.List;

public class FacadeService {

    private final AuthService userService;
    private final TripService tripService;
    private final SeatService seatService;
    private final ReservationService reservationService;
    private final OfficeService officeService;
    private final TransactionManager txManager;

    public FacadeService(AuthService userService,
                            TripService tripService,
                            SeatService seatService,
                            ReservationService reservationService,
                            OfficeService officeService,
                            TransactionManager txManager) {
        this.userService = userService;
        this.tripService = tripService;
        this.seatService = seatService;
        this.reservationService = reservationService;
        this.officeService = officeService;
        this.txManager = txManager;
    }

    public User login(String user, String pass) {
        //return txManager.runWithResult(() -> {
        return userService.login(user, pass);
        //});
    }
    public void logout() {
        userService.logout();
    }

    public List<Trip> searchTrips(String destination, LocalDateTime from, LocalDateTime to) {
        return tripService.search(destination, from, to);
    }

    public List<Seat> getSeatsForTrip(Long tripId) {
        return seatService.getByTripId(tripId);
    }

    public void makeReservationForSeats(String clientName, List<Seat> chosenSeats, Long userId) {
        txManager.run(() -> {
            reservationService.reserveSeats(clientName, chosenSeats, userId);
        });
    }

    public void cancelReservation(Long reservationId) {
        txManager.run(()-> {
            reservationService.cancel(reservationId);
        });
    }

    public List<Reservation> getAllReservations() {
        return reservationService.getAll();
    }

}