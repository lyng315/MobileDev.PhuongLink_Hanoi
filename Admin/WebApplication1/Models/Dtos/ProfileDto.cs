namespace WebApplication1.Models.Dtos
{
    public class ProfileDto
    {
        public string Email { get; set; } = "";
        public string FullName { get; set; } = "";
        public string PhoneNumber { get; set; } = "";   // nếu chưa có trong Firestore sẽ để trống
        public string Address { get; set; } = "";
        public string RoleName { get; set; } = "";
    }
}
