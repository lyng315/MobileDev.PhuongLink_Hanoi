using System.ComponentModel.DataAnnotations;

namespace WebApplication1.Models.Dtos
{
    public class ChangePasswordDto
    {
        [Required]
        [DataType(DataType.Password)]
        [Display(Name = "Mật khẩu cũ")]
        public string CurrentPassword { get; set; } = "";

        [Required]
        [MinLength(6)]
        [DataType(DataType.Password)]
        [Display(Name = "Mật khẩu mới")]
        public string NewPassword { get; set; } = "";

        [Required]
        [Compare("NewPassword", ErrorMessage = "Mật khẩu xác nhận không khớp")]
        [DataType(DataType.Password)]
        [Display(Name = "Xác nhận mật khẩu")]
        public string ConfirmPassword { get; set; } = "";
    }
}
