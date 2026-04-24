package Network;

import java.net.InetSocketAddress;

public class ConnectionSession {
    private final String connectionId;
    private final InetSocketAddress udpAddress;
    private Long userId;

    public ConnectionSession(String connectionId, InetSocketAddress udpAddress) {
        this.connectionId = connectionId;
        this.udpAddress = udpAddress;
        this.userId = null;
    }

    public String getConnectionId() { return connectionId; }
    public InetSocketAddress getUdpAddress() { return udpAddress; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public boolean isLoggedIn() { return userId != null; }
}