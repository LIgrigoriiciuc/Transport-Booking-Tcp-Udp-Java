package GUI;
import GUI.Controller.BaseController;
import GUI.Controller.MainWindowController;
import Network.Dto.ResponseDto.UserDTO;
import Network.NetworkProxy;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager implements INavigationListener {

    private final NetworkProxy proxy;
    private Stage mainStage;
    private BaseController currentController;
    private UserDTO currentUser;

    public SceneManager(NetworkProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public void onLoginSuccess(UserDTO user) {
        this.currentUser = user;
        showMainWindow(mainStage, user);
    }

    @Override
    public void onLogout() {
        showLogin(mainStage);
    }
    public void showLogin(Stage stage) {
        this.mainStage = stage;
        initScene(stage, "/fxml/Login.fxml", "Login", 380, 280, null);
    }

    private void showMainWindow(Stage stage, UserDTO user) {
        initScene(stage, "/fxml/MainWindow.fxml",
                "Reservations | Office: " + user.getOfficeAdress()
                        + " | Worker: " + user.getFullName(),
                1150, 750, user);
    }

    private void initScene(Stage stage, String fxmlPath, String title,
                           int width, int height, UserDTO user) {
        try {
            if (currentController != null)
                currentController.cleanup();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller instanceof BaseController base) {
                base.setService(proxy);
                base.setNavigationListener(this);
                if (base instanceof MainWindowController mwc && user != null)
                    mwc.setCurrentUser(user);
                currentController = base;
            }

            stage.setTitle(title);
            stage.setScene(new Scene(root, width, height));
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("Error loading: " + fxmlPath, e);
        }
    }
}