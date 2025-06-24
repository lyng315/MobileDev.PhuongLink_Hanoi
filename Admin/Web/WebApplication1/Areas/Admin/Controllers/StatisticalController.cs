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

        public async Task<IActionResult> Index(string range = "month")
        {
            var usersSnap = await _db.Collection("users").GetSnapshotAsync();
            var commentsSnap = await _db.Collection("comments").GetSnapshotAsync();
            var postsSnap = await _db.Collection("posts").GetSnapshotAsync();
            var notificationsSnap = await _db.Collection("notificationHistory").GetSnapshotAsync();
           


            var now = DateTime.UtcNow;
            var weekStart = now.Date.AddDays(-(int)now.DayOfWeek + 1); // Thứ 2
            var monthStart = new DateTime(now.Year, now.Month, 1);

            int totalUsers = usersSnap.Count;
            int totalComments = commentsSnap.Count;
            int totalPosts = postsSnap.Count;
            int totalNotifications = notificationsSnap.Count;
            int postsThisWeek = postsSnap.Documents
                .Select(d => d.GetValue<Timestamp>("createdAt").ToDateTime())
                .Count(d => d >= weekStart);

            int postsThisMonth = postsSnap.Documents
                .Select(d => d.GetValue<Timestamp>("createdAt").ToDateTime())
                .Count(d => d >= monthStart);

            List<string> labels = new();
            List<int> data = new();

            if (range == "week")
            {
                var weekChart = postsSnap.Documents
                    .Select(d => d.GetValue<Timestamp>("createdAt").ToDateTime())
                    .Where(d => d >= weekStart)
                    .GroupBy(d => d.DayOfWeek)
                    .OrderBy(g => g.Key)
                    .ToDictionary(
                        g => GetVietnameseDay(g.Key),
                        g => g.Count()
                    );

                labels = weekChart.Keys.ToList();
                data = weekChart.Values.ToList();
            }
            else
            {
                var monthChart = postsSnap.Documents
                    .Select(d => d.GetValue<Timestamp>("createdAt").ToDateTime())
                    .Where(d => d >= monthStart)
                    .GroupBy(d => (d.Day - 1) / 7 + 1)
                    .OrderBy(g => g.Key)
                    .ToDictionary(
                        g => $"Tuần {g.Key}",
                        g => g.Count()
                    );

                labels = monthChart.Keys.ToList();
                data = monthChart.Values.ToList();
            }

            var model = new StatisticalViewModel
            {
                TotalUsers = totalUsers,
                TotalComments = totalComments,
                TotalPosts = totalPosts,
                PostsThisWeek = postsThisWeek,
                PostsThisMonth = postsThisMonth,
                TotalNotifications = totalNotifications,
                PostsChartLabels = labels,
                PostsChartData = data
            };

            return View(model);
        }

        private string GetVietnameseDay(DayOfWeek day)
        {
            return day switch
            {
                DayOfWeek.Monday => "Thứ 2",
                DayOfWeek.Tuesday => "Thứ 3",
                DayOfWeek.Wednesday => "Thứ 4",
                DayOfWeek.Thursday => "Thứ 5",
                DayOfWeek.Friday => "Thứ 6",
                DayOfWeek.Saturday => "Thứ 7",
                DayOfWeek.Sunday => "Chủ nhật",
                _ => "?"
            };
        }
    }
}
