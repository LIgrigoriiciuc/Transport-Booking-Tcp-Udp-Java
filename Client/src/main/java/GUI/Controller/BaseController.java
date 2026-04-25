package GUI.Controller;

import GUI.INavigationListener;
import Network.INetworkService;

public interface BaseController {
    void setService(INetworkService service);
    void setNavigationListener(INavigationListener listener);
    default void cleanup() {}
}
