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
            // Lấy tất cả bài viết để join
            var postSnap = await _db.Collection("posts").GetSnapshotAsync();
            var postDict = postSnap.Documents.ToDictionary(
                d => d.Id,  // postId
                d => d.GetValue<string>("title")  // giả sử field là "title"
            );

            // Lấy danh sách thông báo
            var notifSnap = await _db.Collection("notificationHistory").GetSnapshotAsync();

            var list = notifSnap.Documents.Select(d =>
            {
                var postId = d.GetValue<string>("postId");

                return new NotificationHistoryDto
                {
                    NotificationId = d.GetValue<string>("notificationId"),
                    PostId = postId,
                    PostTitle = postDict.ContainsKey(postId) ? postDict[postId] : "(Không tìm thấy)",
                    RecipientId = d.GetValue<string>("recipientId"),
                    SentAt = d.GetValue<Timestamp>("sentAt").ToDateTime(),
                    Status = d.GetValue<string>("status")
                };
            }).ToList();

            return View(list);
        }
    }
}
