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
        public PostCategoryController(FirestoreDb db) => _db = db;

        public async Task<IActionResult> Index(string keyword)
        {
            var snap = await _db.Collection("postCategories").GetSnapshotAsync();
            var list = snap.Documents
                           .Select(d => {
                               var dto = d.ConvertTo<PostCategoryDto>();
                               dto.Id = d.Id;
                               return dto;
                           })
                           .ToList();

            if (!string.IsNullOrWhiteSpace(keyword))
                list = list
                    .Where(x => x.Name.Contains(keyword, StringComparison.OrdinalIgnoreCase))
                    .ToList();

            return View(list);
        }

        public IActionResult Create() => View(new PostCategoryDto());

        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(PostCategoryDto dto)
        {
            if (!ModelState.IsValid) return View(dto);
            var doc = _db.Collection("postCategories").Document();
            await doc.SetAsync(new { dto.Name });
            return RedirectToAction(nameof(Index));
        }

        public async Task<IActionResult> Edit(string id)
        {
            var snap = await _db.Collection("postCategories").Document(id).GetSnapshotAsync();
            if (!snap.Exists) return NotFound();
            var dto = snap.ConvertTo<PostCategoryDto>();
            dto.Id = id;
            return View(dto);
        }

        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(PostCategoryDto dto)
        {
            if (!ModelState.IsValid) return View(dto);
            var docRef = _db.Collection("postCategories").Document(dto.Id);
            await docRef.UpdateAsync("Name", dto.Name);
            return RedirectToAction(nameof(Index));
        }

        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(string id)
        {
            await _db.Collection("postCategories").Document(id).DeleteAsync();
            return RedirectToAction(nameof(Index));
        }
    }
}