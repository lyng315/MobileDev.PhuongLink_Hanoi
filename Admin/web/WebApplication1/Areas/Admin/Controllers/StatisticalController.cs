using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using WebApplication1.Models.ViewModels;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class StatisticalController : Controller
    {
        private readonly FirestoreDb _db;

        public StatisticalController(FirestoreDb db)
        {
            _db = db;
        }

        public async Task<IActionResult> Index()
        {
            var usersSnap = await _db.Collection("users").GetSnapshotAsync();
            var commentsSnap = await _db.Collection("comments").GetSnapshotAsync();
            var postsSnap = await _db.Collection("posts").GetSnapshotAsync();

            var now = DateTime.UtcNow;
            var oneWeekAgo = now.AddDays(-7);
            var oneMonthAgo = now.AddMonths(-1);

            int totalUsers = usersSnap.Count;
            int totalComments = commentsSnap.Count;
            int totalPosts = postsSnap.Count;

            int postsThisWeek = postsSnap.Documents
                .Select(d => d.GetValue<Timestamp>("createdAt").ToDateTime())
                .Count(d => d >= oneWeekAgo);

            int postsThisMonth = postsSnap.Documents
                .Select(d => d.GetValue<Timestamp>("createdAt").ToDateTime())
                .Count(d => d >= oneMonthAgo);

            var chartData = postsSnap.Documents
                .Select(d => d.GetValue<Timestamp>("createdAt").ToDateTime().ToString("yyyy-MM-dd"))
                .GroupBy(date => date)
                .OrderBy(g => g.Key)
                .ToDictionary(g => g.Key, g => g.Count());

            var model = new StatisticalViewModel
            {
                TotalUsers = totalUsers,
                TotalComments = totalComments,
                TotalPosts = totalPosts,
                PostsThisWeek = postsThisWeek,
                PostsThisMonth = postsThisMonth,
                PostsChartLabels = chartData.Keys.ToList(),
                PostsChartData = chartData.Values.ToList()
            };

            return View(model);
        }
    }
}
