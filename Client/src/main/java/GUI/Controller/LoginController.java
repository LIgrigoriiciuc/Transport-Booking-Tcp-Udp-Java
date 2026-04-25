package GUI.Controller;

import GUI.INavigationListener;
import Network.Dto.RequestDto.LoginDTO;
import Network.Dto.ResponseDto.UserDTO;
import Network.INetworkService;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController implements BaseController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    private INetworkService service;
    private INavigationListener navigation;

    @Override
    public void setService(INetworkService service) {
        this.service = service;
    }
    @Override
    public void setNavigationListener(INavigationListener listener) { this.navigation = listener; }

    @FXML
    public void initialize() {
        EventHandler<KeyEvent> enterHandler = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
                event.consume();
            }
        };
        usernameField.addEventHandler(KeyEvent.KEY_PRESSED, enterHandler);
        passwordField.addEventHandler(KeyEvent.KEY_PRESSED, enterHandler);
    }

    @FXML
    private void handleLogin() {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            UserDTO user = service.login(new LoginDTO(username, password, 0));
            navigation.onLoginSuccess(user);
        }
        catch (Exception e) {
            errorLabel.setText(e.getMessage());
            passwordField.clear();
        }
    }
}
