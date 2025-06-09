using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using WebApplication1.Models.Dtos;

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

            var userMap = usersSnap.Documents.ToDictionary(d => d.Id, d => d.GetValue<string>("fullName"));
            var categoryMap = categoriesSnap.Documents.ToDictionary(d => d.Id, d => d.GetValue<string>("name"));
            var regionMap = regionsSnap.Documents.ToDictionary(d => d.Id, d => d.GetValue<string>("name"));

            var list = postsSnap.Documents.Select(d =>
            {
                var dto = d.ConvertTo<PostsDTO>();
                dto.Id = d.Id;

                dto.AuthorName = userMap.GetValueOrDefault(dto.AuthorUserId ?? "");
                dto.CategoryName = categoryMap.GetValueOrDefault(dto.CategoryId ?? "");
                dto.RegionName = regionMap.GetValueOrDefault(dto.TargetRegionId ?? "");

                return dto;
            }).ToList();

            return View(list);
        }

        // GET: /Admin/Posts/Create
        public async Task<IActionResult> Create()
        {
            ViewBag.Users = await _db.Collection("users").GetSnapshotAsync();
            ViewBag.Categories = await _db.Collection("postCategories").GetSnapshotAsync();
            ViewBag.Regions = await _db.Collection("regions").GetSnapshotAsync();
            return View(new PostsDTO());
        }

        // POST: /Admin/Posts/Create
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(PostsDTO dto)
        {
            if (!ModelState.IsValid)
            {
                await LoadSelectLists();
                return View(dto);
            }

            var snap = await _db.Collection(COLL).GetSnapshotAsync();
            var max = snap.Documents
                          .Select(d => d.Id)
                          .Where(id => id.StartsWith("post"))
                          .Select(id => int.TryParse(id.Substring("post".Length), out var n) ? n : 0)
                          .DefaultIfEmpty(0)
                          .Max();

            var newId = $"post{max + 1:00}";

            dto.CreatedAt = Timestamp.FromDateTime(DateTime.UtcNow);
            dto.EditedAt = dto.CreatedAt;

            await _db.Collection(COLL).Document(newId).SetAsync(dto);
            return RedirectToAction(nameof(Index));
        }

        // GET: /Admin/Posts/Edit/{id}
        public async Task<IActionResult> Edit(string id)
        {
            if (string.IsNullOrEmpty(id)) return NotFound();

            var snap = await _db.Collection(COLL).Document(id).GetSnapshotAsync();
            if (!snap.Exists) return NotFound();

            var dto = snap.ConvertTo<PostsDTO>();
            dto.Id = snap.Id;

            await LoadSelectLists();
            return View(dto);
        }

        // POST: /Admin/Posts/Edit
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(PostsDTO dto)
        {
            if (!ModelState.IsValid)
            {
                await LoadSelectLists();
                return View(dto);
            }

            dto.EditedAt = Timestamp.FromDateTime(DateTime.UtcNow);

            var updateDict = new Dictionary<string, object>
            {
                ["title"] = dto.Title,
                ["content"] = dto.Content,
                ["status"] = dto.Status,
                ["authorUserId"] = dto.AuthorUserId,
                ["categoryId"] = dto.CategoryId,
                ["targetRegionId"] = dto.TargetRegionId,
                ["urgencyLevel"] = dto.UrgencyLevel,
                ["editedAt"] = dto.EditedAt
            };

            await _db.Collection(COLL).Document(dto.Id).UpdateAsync(updateDict);
            return RedirectToAction(nameof(Index));
        }

        // POST: /Admin/Posts/Delete/{id}
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(string id)
        {
            if (string.IsNullOrEmpty(id)) return BadRequest();
            await _db.Collection(COLL).Document(id).DeleteAsync();
            return RedirectToAction(nameof(Index));
        }

        private async Task LoadSelectLists()
        {
            ViewBag.Users = await _db.Collection("users").GetSnapshotAsync();
            ViewBag.Categories = await _db.Collection("postCategories").GetSnapshotAsync();
            ViewBag.Regions = await _db.Collection("regions").GetSnapshotAsync();
        }
    }
}
