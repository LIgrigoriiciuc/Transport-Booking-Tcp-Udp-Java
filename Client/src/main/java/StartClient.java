import Controller.SceneManager;
import Network.NetworkProxy;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StartClient extends Application {

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
                System.err.println("client.properties not found, using defaults");
            }
        } catch (IOException e) {
            System.err.println("Error reading client.properties: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid port in client.properties, using default: " + DEFAULT_PORT);
        }

        proxy = new NetworkProxy(host, port);
        try {
            proxy.connect();
        } catch (IOException e) {
            System.err.println("Cannot connect to server: " + e.getMessage());
            return;
        }

        SceneManager sceneManager = new SceneManager(proxy);
        sceneManager.showLogin(primaryStage);

        primaryStage.setOnCloseRequest(event -> {
            try {
                proxy.disconnect();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}