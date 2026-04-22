package Network.Dto.ResponseDto;

public class UserDTO {
    private Long id;
    private String fullName;
    private String officeAdress;
    public UserDTO() { }
    public UserDTO(Long id, String fullName, String officeAdress) {
        this.id = id;
        this.fullName = fullName;
        this.officeAdress = officeAdress;
    }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getOfficeAdress() { return officeAdress; }
}
