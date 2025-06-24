// Models/ViewModels/PostDetailsViewModel.cs
using System.Collections.Generic;
using WebApplication1.Models.Dtos;

namespace WebApplication1.Models.ViewModels
{
    public class PostDetailsViewModel
    {
        public PostsDTO Post { get; set; } = new PostsDTO();
        public List<CommentDto> Comments { get; set; } = new List<CommentDto>();
    }
}
