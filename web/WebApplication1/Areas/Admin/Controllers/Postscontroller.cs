using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using WebApplication1.Models.Dtos;
using WebApplication1.Models.ViewModels;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class PostsController : Controller
    {
        private readonly FirestoreDb _db;
        private const string COLL = "posts";

        public PostsController(FirestoreDb db)
        {
            _db = db;
        }

        // GET: /Admin/Posts
        public async Task<IActionResult> Index()
        {
            var postsSnap = await _db.Collection(COLL).GetSnapshotAsync();
            var usersSnap = await _db.Collection("users").GetSnapshotAsync();
            var categoriesSnap = await _db.Collection("postCategories").GetSnapshotAsync();
            var regionsSnap = await _db.Collection("regions").GetSnapshotAsync();

            // Build lookup maps
            var userMap = usersSnap.Documents.ToDictionary(d => d.Id, d => d.GetValue<string>("fullName"));
            var categoryMap = categoriesSnap.Documents.ToDictionary(d => d.Id, d => d.GetValue<string>("name"));
            var regionMap = regionsSnap.Documents.ToDictionary(d => d.Id, d => d.GetValue<string>("name"));

            var list = postsSnap.Documents
                .Select(d =>
                {
                    var dto = d.ConvertTo<PostsDTO>();
                    dto.Id = d.Id;
                    dto.AuthorName = userMap.GetValueOrDefault(dto.AuthorUserId ?? "");
                    dto.CategoryName = categoryMap.GetValueOrDefault(dto.CategoryId ?? "");
                    dto.RegionName = regionMap.GetValueOrDefault(dto.TargetRegionId ?? "");
                    return dto;
                })
                .ToList();

            return View(list);
        }

        // GET: /Admin/Posts/Details/{id}
        public async Task<IActionResult> Details(string id)
        {
            if (string.IsNullOrEmpty(id)) return NotFound();

            // 1) Load post document
            var postSnap = await _db.Collection(COLL).Document(id).GetSnapshotAsync();
            if (!postSnap.Exists) return NotFound();

            var postDto = postSnap.ConvertTo<PostsDTO>();
            postDto.Id = postSnap.Id;

            // → Kết hợp thumbnailUrl vào danh sách ImageUrls
            postDto.ImageUrls = postDto.ImageUrls ?? new List<string>();
            if (!string.IsNullOrEmpty(postDto.ThumbnailUrl))
            {
                postDto.ImageUrls.Insert(0, postDto.ThumbnailUrl);
            }

            // Lookup author, category, region
            var userSnap = await _db.Collection("users").Document(postDto.AuthorUserId ?? "").GetSnapshotAsync();
            postDto.AuthorName = userSnap.Exists ? userSnap.GetValue<string>("fullName") : "";

            var catSnap = await _db.Collection("postCategories").Document(postDto.CategoryId ?? "").GetSnapshotAsync();
            postDto.CategoryName = catSnap.Exists ? catSnap.GetValue<string>("name") : "";

            var regSnap = await _db.Collection("regions").Document(postDto.TargetRegionId ?? "").GetSnapshotAsync();
            postDto.RegionName = regSnap.Exists ? regSnap.GetValue<string>("name") : "";

            // 2) Load comments và sort
            var commentSnap = await _db.Collection("comments")
                                       .WhereEqualTo("postId", id)
                                       .GetSnapshotAsync();

            var comments = commentSnap.Documents
                .Select(c =>
                {
                    var dto = c.ConvertTo<CommentDto>();
                    dto.Id = c.Id;
                    var commenter = _db.Collection("users")
                                       .Document(dto.AuthorUserId ?? "")
                                       .GetSnapshotAsync().Result;
                    dto.AuthorName = commenter.Exists ? commenter.GetValue<string>("fullName") : "";
                    return dto;
                })
                .OrderBy(c => c.CreatedAt.ToDateTime())
                .ToList();

            // 3) Build ViewModel
            var vm = new PostDetailsViewModel
            {
                Post = postDto,
                Comments = comments
            };

            return View(vm);
        }

        // POST: /Admin/Posts/Delete/{id}
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(string id)
        {
            if (string.IsNullOrEmpty(id)) return BadRequest();

            await _db.Collection(COLL).Document(id).DeleteAsync();
            return RedirectToAction(nameof(Index));
        }
    }
}
