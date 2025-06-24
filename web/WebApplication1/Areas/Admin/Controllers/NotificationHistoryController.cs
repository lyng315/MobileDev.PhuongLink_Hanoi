using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using WebApplication1.Models.Dtos;
using System;
using System.Linq;
using System.Threading.Tasks;
using WebApplication1.Models.Dtos;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class NotificationHistoryController : Controller
    {
        private readonly FirestoreDb _db;

        public NotificationHistoryController(FirestoreDb db)
        {
            _db = db;
        }

        public async Task<IActionResult> Index()
        {
            // Lấy bài viết
            var postSnap = await _db.Collection("posts").GetSnapshotAsync();
            var postDict = postSnap.Documents.ToDictionary(
                d => d.Id,
                d => d.GetValue<string>("title")
            );

            // Lấy người dùng và tạo từ điển để tra cứu
            var userSnap = await _db.Collection("users").GetSnapshotAsync();
            var userDict = userSnap.Documents.ToDictionary(
                d => d.Id, // chính là IdUser
                d => d.GetValue<string>("fullName") // lấy tên đầy đủ
            );

            // Lấy lịch sử thông báo
            var notifSnap = await _db.Collection("notificationHistory").GetSnapshotAsync();

            var list = notifSnap.Documents.Select(d =>
            {
                var postId = d.GetValue<string>("postId");
                var recipientId = d.GetValue<string>("recipientId");

                // Tra cứu tên người nhận từ userDict
                var recipientName = userDict.ContainsKey(recipientId) ? userDict[recipientId] : "(Không tìm thấy)";

                return new NotificationHistoryDto
                {
                    PostId = postId,
                    PostTitle = postDict.ContainsKey(postId) ? postDict[postId] : "(Không tìm thấy)",
                    RecipientId = recipientId,
                    RecipientName = recipientName, // Gán tên người nhận
                    SentAt = d.GetValue<Timestamp>("sentAt").ToDateTime()
                };
            }).ToList();

            return View(list);
        }





    }
}
