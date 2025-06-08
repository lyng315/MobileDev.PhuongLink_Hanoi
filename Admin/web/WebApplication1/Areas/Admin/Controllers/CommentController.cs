using System;
using System.Linq;
using System.Threading.Tasks;
using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using WebApplication1.Models.Dtos;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class CommentController : Controller
    {
        private readonly FirestoreDb _db;
        private const string COLL = "comments";

        public CommentController(FirestoreDb db) => _db = db;

        // GET: /Admin/Comment
        public async Task<IActionResult> Index()
        {
            // 1) Lấy map postId → title
            var postSnap = await _db.Collection("posts").GetSnapshotAsync();
            var postMap = postSnap.Documents
                                  .ToDictionary(d => d.Id,
                                                d => d.GetValue<string>("title"));

            // 2) Lấy map userId → email
            var userSnap = await _db.Collection("users").GetSnapshotAsync();
            var userMap = userSnap.Documents
                                  .ToDictionary(d => d.Id,
                                                d => d.GetValue<string>("email"));

            // 3) Lấy bình luận
            var snap = await _db.Collection(COLL)
                                .OrderBy("createdAt")
                                .GetSnapshotAsync();

            var list = snap.Documents
                           .Select(d =>
                           {
                               var c = d.ConvertTo<CommentDto>();
                               c.Id = d.Id;
                               // gán tiêu đề bài đăng
                               c.PostTitle = postMap.TryGetValue(c.PostId, out var t)
                                               ? t
                                               : c.PostId;
                               // gán email người bình luận
                               c.AuthorEmail = userMap.TryGetValue(c.AuthorUserId, out var e)
                                               ? e
                                               : c.AuthorUserId;
                               return c;
                           })
                           .ToList();

            return View(list);
        }

        // POST: /Admin/Comment/Delete/{id}
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(string id)
        {
            await _db.Collection(COLL).Document(id).DeleteAsync();
            return RedirectToAction(nameof(Index));
        }

        // POST: /Admin/Comment/Create
        // (API for app) – giữ nguyên logic tự sinh ID như trước
        [HttpPost]
        [Route("Admin/Comment/Create")]
        public async Task<IActionResult> Create([FromBody] CommentDto dto)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            // Tự sinh ID commentNN
            var all = await _db.Collection(COLL).GetSnapshotAsync();
            var max = all.Documents
                         .Select(d => d.Id)
                         .Where(id => id.StartsWith("comment"))
                         .Select(id =>
                         {
                             var num = id.Substring("comment".Length);
                             return int.TryParse(num, out var n) ? n : 0;
                         })
                         .DefaultIfEmpty(0)
                         .Max();
            var newId = $"comment{max + 1:00}";

            dto.CreatedAt = Timestamp.FromDateTime(DateTime.UtcNow);
            await _db.Collection(COLL)
                     .Document(newId)
                     .SetAsync(dto);

            return Ok(new { id = newId });
        }
    }
}
