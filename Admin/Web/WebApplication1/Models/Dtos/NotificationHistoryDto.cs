namespace WebApplication1.Models.Dtos
{
    public class NotificationHistoryDto
    {
        public string NotificationId { get; set; }
        public string PostId { get; set; }
        public string PostTitle { get; set; }  // ✅ thêm trường này
        public string RecipientId { get; set; }
        public DateTime SentAt { get; set; }
        public string Status { get; set; }
        public string RecipientName { get; set; } = "";  // Thêm để hiển thị tên

    }

}
