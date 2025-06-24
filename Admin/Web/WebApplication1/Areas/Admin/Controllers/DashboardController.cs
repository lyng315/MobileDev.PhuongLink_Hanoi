using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Linq;
using System.Threading.Tasks;
using WebApplication1.Models.ViewModels;
using WebApplication1.Models.Dtos;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class DashboardController : Controller
    {
        private readonly FirestoreDb _db;

        public DashboardController(FirestoreDb db)
        {
            _db = db;
        }

        // GET: /Admin/Dashboard
        public async Task<IActionResult> Index(string searchQuery)
        {
            // Lấy thông tin bài viết, bình luận và người dùng từ Firestore
            var postSnap = await _db.Collection("posts").GetSnapshotAsync();
            var commentSnap = await _db.Collection("comments").GetSnapshotAsync();
            var userSnap = await _db.Collection("users").GetSnapshotAsync();

            var now = DateTime.UtcNow;
            var thisWeekStart = now.Date.AddDays(-(int)now.DayOfWeek + 1); // Thứ 2 tuần này
            var lastWeekStart = thisWeekStart.AddDays(-7);
            var lastWeekEnd = thisWeekStart;

            // Tính toán số lượng bài viết, bình luận và người dùng trong tuần này
            int postsThisWeek = postSnap.Documents.Count(d =>
                d.TryGetValue<Timestamp>("createdAt", out Timestamp createdAt) &&
                createdAt.ToDateTime() >= thisWeekStart);

            int commentsThisWeek = commentSnap.Documents.Count(d =>
                d.TryGetValue<Timestamp>("createdAt", out Timestamp createdAt) &&
                createdAt.ToDateTime() >= thisWeekStart);

            int usersThisWeek = userSnap.Documents.Count(d =>
                d.TryGetValue<Timestamp>("createdAt", out Timestamp createdAt) &&
                createdAt.ToDateTime() >= thisWeekStart);

            // Tính toán số lượng bài viết, bình luận và người dùng trong tuần trước
            int postsLastWeek = postSnap.Documents.Count(d =>
                d.TryGetValue<Timestamp>("createdAt", out Timestamp createdAt) &&
                createdAt.ToDateTime() >= lastWeekStart &&
                createdAt.ToDateTime() < lastWeekEnd);

            int commentsLastWeek = commentSnap.Documents.Count(d =>
                d.TryGetValue<Timestamp>("createdAt", out Timestamp createdAt) &&
                createdAt.ToDateTime() >= lastWeekStart &&
                createdAt.ToDateTime() < lastWeekEnd);

            int usersLastWeek = userSnap.Documents.Count(d =>
                d.TryGetValue<Timestamp>("createdAt", out Timestamp createdAt) &&
                createdAt.ToDateTime() >= lastWeekStart &&
                createdAt.ToDateTime() < lastWeekEnd);

            // Lấy 3 bài viết mới nhất và thêm thông tin ảnh
            var latestPosts = postSnap.Documents
                .OrderByDescending(d => d.GetValue<Timestamp>("createdAt").ToDateTime()) // Sắp xếp theo thời gian
                .Take(3)
                .Select(d =>
                {
                    var dto = d.ConvertTo<PostsDTO>();
                    dto.Id = d.Id;
                    dto.ThumbnailUrl = d.GetValue<string>("thumbnailUrl");  // Lấy URL ảnh thumbnail
                    return dto;
                }).ToList();

            // Tạo ViewModel để truyền dữ liệu cho view
            var model = new CommunityDashboardViewModel
            {
                TotalPosts = postsThisWeek,
                TotalComments = commentsThisWeek,
                TotalUsers = usersThisWeek,
                GrowthPostPercent = CalculateGrowth(postsLastWeek, postsThisWeek),
                GrowthCommentPercent = CalculateGrowth(commentsLastWeek, commentsThisWeek),
                GrowthUserPercent = CalculateGrowth(usersLastWeek, usersThisWeek),
                LatestPosts = latestPosts
            };

            return View(model);
        }

        // Phương thức tính toán tỷ lệ tăng trưởng (growth)
        private int CalculateGrowth(int lastWeek, int thisWeek)
        {
            if (lastWeek == 0)
                return thisWeek > 0 ? 100 : 0;

            return (int)Math.Round(((double)(thisWeek - lastWeek) / lastWeek) * 100);
        }

        // GET: /Admin/Dashboard/LoadMorePosts
        public async Task<IActionResult> LoadMorePosts(string lastPostTimestamp)
        {
            // Parse the timestamp từ tham số lastPostTimestamp
            Timestamp lastTimestamp = Timestamp.FromDateTime(DateTime.Parse(lastPostTimestamp).ToUniversalTime());

            // Lấy các bài viết từ Firestore sau thời gian bài viết cuối cùng
            var query = _db.Collection("posts")
                .OrderByDescending("createdAt")
                .StartAfter(lastTimestamp) // Bắt đầu sau bài viết cuối cùng được hiển thị
                .Limit(3); // Giới hạn lấy thêm 3 bài viết

            var postSnap = await query.GetSnapshotAsync();

            var latestPosts = postSnap.Documents
                .Select(d =>
                {
                    var dto = d.ConvertTo<PostsDTO>();
                    dto.Id = d.Id;
                    dto.ThumbnailUrl = d.GetValue<string>("thumbnailUrl");  // Giới hạn ảnh thumbnail
                    return dto;
                }).ToList();

            return PartialView("_LatestPosts", latestPosts);  // Trả về các bài viết mới được tải thêm
        }



    }
}