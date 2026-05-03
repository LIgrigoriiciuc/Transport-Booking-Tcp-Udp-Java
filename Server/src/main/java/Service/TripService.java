package Service;


import Domain.Trip;
import Repository.Filter;
import Repository.TripRepository;
import Util.DateTimeUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.time.LocalDateTime;
import java.util.List;

public class TripService extends GenericService<Long, Trip> {

    private static final Logger logger = LogManager.getLogger(TripService.class);

    public TripService(TripRepository repository) {
        super(repository);
    }

    public List<Trip> search(String destination, LocalDateTime from, LocalDateTime to) {
        logger.info("Searching trips with destination: {}, from: {}, to: {}", destination, from, to);
        Filter f = new Filter();
        if (destination != null && !destination.isBlank())
            f.addLikeFilter("destination", destination);
        if (from != null && to != null)
            f.addRangeFilter("time",
                    DateTimeUtils.format(from),
                    DateTimeUtils.format(to)
            );
        List<Trip> trips = f.isEmpty() ? repository.getAll() : repository.filter(f);
        return trips;
    }
}