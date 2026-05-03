import Network.ConcurrentServer;
import Network.NetworkServiceImpl;
import Network.UdpPusher;
import Repository.*;
import Service.*;
import Service.TransactionsLogic.TransactionManager;
import Util.ConnectionHolder;
import Util.DatabaseConnection;
import Util.DatabaseInitializer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StartServer {

    private static final Logger logger = LogManager.getLogger(StartServer.class);
    private static final int DEFAULT_PORT = 65535;
    public static void main(String[] args) {
        int port = loadPort();
        logger.info("Starting server on port {}", port);
        DatabaseInitializer.initializeDatabase();

        SeatRepository seatRepo = new SeatRepository();
        TripRepository tripRepo = new TripRepository();
        ReservationRepository resRepo = new ReservationRepository();
        UserRepository userRepo = new UserRepository();
        OfficeRepository officeRepo = new OfficeRepository();
        OfficeService officeService = new OfficeService(officeRepo);
        AuthService userService = new AuthService(userRepo);
        TripService tripService = new TripService(tripRepo);
        SeatService seatService = new SeatService(seatRepo);
        ReservationService resService = new ReservationService(resRepo, seatService);
        TransactionManager txManager = new TransactionManager();
        FacadeService facadeService = new FacadeService(userService, tripService, seatService, resService, officeService, txManager);
        try{
            UdpPusher udpPusher = new UdpPusher();
            NetworkServiceImpl networkService = new NetworkServiceImpl(facadeService, udpPusher);
            ConcurrentServer server = new ConcurrentServer(port, networkService);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Stopping server");
                try {
                    server.stop();
                } catch (IOException ignored) {}
                udpPusher.close();
                DatabaseConnection.close();
            }));
            server.start();
        }
        catch (IOException ignored){
            logger.error("Failed to start server", ignored);
        }
    }
    private static int loadPort() {
        Properties props = new Properties();
        try (InputStream in = StartServer.class.getClassLoader()
                .getResourceAsStream("server.properties")) {
            if (in != null) props.load(in);
        } catch (IOException ignored) {
        }
        try {
            return Integer.parseInt(
                    props.getProperty("server.port", String.valueOf(DEFAULT_PORT)));
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }
}
