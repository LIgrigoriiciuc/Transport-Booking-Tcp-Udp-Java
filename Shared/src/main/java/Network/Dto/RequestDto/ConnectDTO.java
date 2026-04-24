package Network.Dto.RequestDto;

public class ConnectDTO {
    private int udpPort;
    public ConnectDTO() {}
    public ConnectDTO(int udpPort) { this.udpPort = udpPort; }
    public int getUdpPort() { return udpPort; }
}
