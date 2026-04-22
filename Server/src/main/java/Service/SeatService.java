package Service;


import Domain.Seat;
import Repository.Filter;
import Repository.SeatRepository;

import java.util.List;

public class SeatService extends GenericService<Long, Seat> {
    public SeatService(SeatRepository seatRepository) {
        super(seatRepository);
    }

    public List<Seat> getByTripId(Long tripId) {
        Filter filter = new Filter();
        filter.addFilter("trip_id", tripId);
        return filter(filter);
    }
    public List<Seat> getByReservationId(Long reservationId) {
        Filter filter = new Filter();
        filter.addFilter("reservation_id", reservationId);
        return filter(filter);
    }

}
