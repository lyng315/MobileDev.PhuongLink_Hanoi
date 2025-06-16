using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Linq;
using System.Threading.Tasks;
using WebApplication1.Models.ViewModels;

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

        public async Task<IActionResult> Index()
        {
            var postSnap = await _db.Collection("posts").GetSnapshotAsync();
            var commentSnap = await _db.Collection("comments").GetSnapshotAsync();
            var userSnap = await _db.Collection("users").GetSnapshotAsync();

            var now = DateTime.UtcNow;
            var thisWeekStart = now.Date.AddDays(-(int)now.DayOfWeek + 1); // Thứ 2 tuần này
            var lastWeekStart = thisWeekStart.AddDays(-7);
            var lastWeekEnd = thisWeekStart;

            // Tuần này
            int postsThisWeek = postSnap.Documents.Count(d =>
                d.TryGetValue<Timestamp>("createdAt", out Timestamp createdAt) &&
                createdAt.ToDateTime() >= thisWeekStart);

            int commentsThisWeek = commentSnap.Documents.Count(d =>
                d.TryGetValue<Timestamp>("createdAt", out Timestamp createdAt) &&
                createdAt.ToDateTime() >= thisWeekStart);

            int usersThisWeek = userSnap.Documents.Count(d =>
                d.TryGetValue<Timestamp>("createdAt", out Timestamp createdAt) &&
                createdAt.ToDateTime() >= thisWeekStart);

            // Tuần trước
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

            var model = new CommunityDashboardViewModel
            {
                TotalPosts = postsThisWeek,
                TotalComments = commentsThisWeek,
                TotalUsers = usersThisWeek,
                GrowthPostPercent = CalculateGrowth(postsLastWeek, postsThisWeek),
                GrowthCommentPercent = CalculateGrowth(commentsLastWeek, commentsThisWeek),
                GrowthUserPercent = CalculateGrowth(usersLastWeek, usersThisWeek),
            };

            return View(model);
        }

        private int CalculateGrowth(int lastWeek, int thisWeek)
        {
            if (lastWeek == 0)
                return thisWeek > 0 ? 100 : 0;

            return (int)Math.Round(((double)(thisWeek - lastWeek) / lastWeek) * 100);
        }
    }
}
