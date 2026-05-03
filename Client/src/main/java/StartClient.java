import GUI.SceneManager;
import Network.NetworkProxy;
import javafx.application.Application;
import javafx.stage.Stage;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StartClient extends Application {

    private static final Logger logger = LogManager.getLogger(StartClient.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 65535;
    private NetworkProxy proxy;

    @Override
    public void start(Stage primaryStage) {
        Properties props = new Properties();
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        try (InputStream in = getClass().getResourceAsStream("/client.properties")) {
            if (in != null) {
                props.load(in);
                host = props.getProperty("server.host", DEFAULT_HOST);
                port = Integer.parseInt(props.getProperty("server.port", String.valueOf(DEFAULT_PORT)));
            } else {
                logger.warn("client.properties not found, using defaults");
            }
        } catch (IOException e) {
            logger.warn("Error reading client.properties", e);
        } catch (NumberFormatException e) {
            logger.warn("Invalid port in client.properties, using default", e);
        }

        proxy = new NetworkProxy(host, port);
        try {
            proxy.connect();
            logger.info("Connected to server at {}:{}", host, port);
        } catch (IOException e) {
            logger.error("Failed to connect to server", e);
            return;
        }

        SceneManager sceneManager = new SceneManager(proxy);
        sceneManager.showLogin(primaryStage);

        primaryStage.setOnCloseRequest(event -> {
            try {
                proxy.disconnect();
            } catch (IOException e) {
                logger.error("Error disconnecting from server", e);
                throw new RuntimeException(e);
            }
        });
    }

    public static void main(String[] args) {
        logger.info("Starting client application");
        launch(args);
    }
}