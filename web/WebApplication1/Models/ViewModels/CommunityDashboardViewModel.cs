namespace WebApplication1.Models.ViewModels
{
    public class CommunityDashboardViewModel
    {
        public int TotalPosts { get; set; }
        public int TotalComments { get; set; }
        public int TotalUsers { get; set; }

        public int GrowthPostPercent { get; set; }
        public int GrowthCommentPercent { get; set; }
        public int GrowthUserPercent { get; set; }
    }
}
