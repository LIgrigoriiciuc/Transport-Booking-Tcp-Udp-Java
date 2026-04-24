package Controller;

import Network.Dto.ResponseDto.UserDTO;

public interface INavigationListener {
    void onLoginSuccess(UserDTO user);
    void onLogout();
}
