package Network;

import java.net.InetSocketAddress;

public class ConnectionSession {
    private final InetSocketAddress udpAddress;
    private Long userId;

    public ConnectionSession(InetSocketAddress udpAddress) {
        this.udpAddress = udpAddress;
        this.userId = null;
    }

    public InetSocketAddress getUdpAddress() { return udpAddress; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public boolean isLoggedIn() { return userId != null; }
}