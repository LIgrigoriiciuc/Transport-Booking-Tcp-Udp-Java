package Network.Dto.RequestDto;

public class LoginDTO {
    private String username;
    private String password;
    private int udpPort;
    public LoginDTO() { }
    public LoginDTO(String username, String password, int udpPort) {
        this.username = username;
        this.password = password;
        this.udpPort  = udpPort;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public int getUdpPort() {
        return udpPort;
    }
}
