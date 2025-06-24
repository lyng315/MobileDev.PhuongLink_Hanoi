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

        // Thêm thuộc tính LatestPosts để chứa danh sách 3 bài viết mới nhất
        public List<WebApplication1.Models.Dtos.PostsDTO> LatestPosts { get; set; }

    }

}
