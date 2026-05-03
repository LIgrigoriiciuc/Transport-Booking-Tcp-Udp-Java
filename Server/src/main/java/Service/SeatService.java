package Service;


import Domain.Seat;
import Repository.Filter;
import Repository.SeatRepository;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Optional;

public class SeatService extends GenericService<Long, Seat> {

    private static final Logger logger = LogManager.getLogger(SeatService.class);

    public SeatService(SeatRepository seatRepository) {
        super(seatRepository);
    }

    public List<Seat> getByTripId(Long tripId) {
        logger.info("Getting seats for trip {}", tripId);
        Filter filter = new Filter();
        filter.addFilter("trip_id", tripId);
        return filter(filter);
    }
    public List<Seat> getByReservationId(Long reservationId) {
        logger.info("Getting seats for reservation {}", reservationId);
        Filter filter = new Filter();
        filter.addFilter("reservation_id", reservationId);
        return filter(filter);
    }
    public List<Seat> getFreeByTripId(Long tripId) {
        logger.info("Getting free seats for trip {}", tripId);
        Filter filter = new Filter();
        filter.addFilter("trip_id", tripId);
        filter.addFilter("isReserved", 0);
        return filter(filter);
    }

    public List<Integer> getSeatNumbersByReservation(Long reservationId) {
        logger.info("Getting seat numbers for reservation {}", reservationId);
        Filter filter = new Filter();
        filter.addFilter("reservation_id", reservationId);
        return filter(filter).stream()
                .map(Seat::getNumber)
                .toList();
    }

    public Optional<Long> getTripIdByReservationId(Long reservationId) {
        logger.info("Getting trip id for reservation {}", reservationId);
        return getByReservationId(reservationId).stream()
                .findFirst()
                .map(Seat::getTripId);
    }


}
