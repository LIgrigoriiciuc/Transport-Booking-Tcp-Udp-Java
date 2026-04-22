package Network.Dto.RequestDto;

public class LogoutDTO {
    private Long userId;
    public LogoutDTO() { }
    public LogoutDTO(Long userId) {
        this.userId = userId;
    }
    public Long getUserId() {
        return userId;
    }
}
