namespace WebApplication1.Models.ViewModels
{
    public class StatisticalViewModel
    {
        public int TotalUsers { get; set; }
        public int TotalComments { get; set; }
        public int TotalPosts { get; set; }
        public int PostsThisWeek { get; set; }
        public int PostsThisMonth { get; set; }
        public int TotalNotifications { get; set; }

        public List<string> PostsChartLabels { get; set; } = new();
        public List<int> PostsChartData { get; set; } = new();
    }
}

