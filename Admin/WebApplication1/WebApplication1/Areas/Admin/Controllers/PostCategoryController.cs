using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Linq;
using System.Threading.Tasks;
using WebApplication1.Models.Dtos;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class PostCategoryController : Controller
    {
        private readonly FirestoreDb _db;
        private const string COLL = "postCategories";

        public PostCategoryController(FirestoreDb db)
        {
            _db = db;
        }

        // GET: /Admin/PostCategory?keyword=...
        public async Task<IActionResult> Index(string keyword)
        {
            var snap = await _db.Collection(COLL).GetSnapshotAsync();
            var list = snap.Documents
                .Select(d =>
                {
                    var dto = d.ConvertTo<PostCategoryDto>();
                    dto.Id = d.Id;
                    return dto;
                })
                .ToList();

            if (!string.IsNullOrWhiteSpace(keyword))
            {
                list = list
                    .Where(x => x.Name.Contains(keyword, StringComparison.OrdinalIgnoreCase))
                    .ToList();
            }

            ViewBag.Keyword = keyword ?? "";
            return View(list);
        }

        // GET: /Admin/PostCategory/Create
        public IActionResult Create()
        {
            return View(new PostCategoryDto());
        }

        // POST: /Admin/PostCategory/Create
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(PostCategoryDto dto)
        {
            if (!ModelState.IsValid)
                return View(dto);

            // 1) Lấy tất cả IDs hiện có
            var snap = await _db.Collection(COLL).GetSnapshotAsync();
            var maxNum = snap.Documents
                .Select(d => d.Id)
                .Where(id => id.StartsWith("category"))
                .Select(id =>
                {
                    var tail = id.Substring("category".Length);
                    return int.TryParse(tail, out var n) ? n : 0;
                })
                .DefaultIfEmpty(0)
                .Max();

            // 2) Tạo ID mới theo pattern categoryNN
            var next = maxNum + 1;
            var newId = $"category{next:00}";

            // 3) Ghi document với ID tự sinh
            await _db.Collection(COLL)
                     .Document(newId)
                     .SetAsync(dto);

            return RedirectToAction(nameof(Index));
        }

        // GET: /Admin/PostCategory/Edit/{id}
        public async Task<IActionResult> Edit(string id)
        {
            if (string.IsNullOrEmpty(id))
                return NotFound();

            var snap = await _db.Collection(COLL).Document(id).GetSnapshotAsync();
            if (!snap.Exists)
                return NotFound();

            var dto = snap.ConvertTo<PostCategoryDto>();
            dto.Id = snap.Id;
            return View(dto);
        }

        // POST: /Admin/PostCategory/Edit
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(PostCategoryDto dto)
        {
            if (!ModelState.IsValid)
                return View(dto);

            await _db.Collection(COLL)
                     .Document(dto.Id)
                     .UpdateAsync("name", dto.Name);

            return RedirectToAction(nameof(Index));
        }

        // POST: /Admin/PostCategory/Delete/{id}
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(string id)
        {
            if (string.IsNullOrEmpty(id))
                return BadRequest();

            await _db.Collection(COLL)
                     .Document(id)
                     .DeleteAsync();

            return RedirectToAction(nameof(Index));
        }
    }
}
