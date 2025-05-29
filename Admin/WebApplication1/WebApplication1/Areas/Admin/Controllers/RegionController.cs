using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Linq;
using System.Threading.Tasks;
using WebApplication1.Models.Dtos;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class RegionController : Controller
    {
        private readonly FirestoreDb _db;
        private const string COLL = "regions";

        public RegionController(FirestoreDb db)
        {
            _db = db;
        }

        // GET: /Admin/Region
        public async Task<IActionResult> Index()
        {
            var snap = await _db.Collection(COLL).GetSnapshotAsync();
            var list = snap.Documents
                           .Select(d =>
                           {
                               var dto = d.ConvertTo<RegionDto>();
                               dto.Id = d.Id;
                               return dto;
                           })
                           .ToList();
            return View(list);
        }

        // GET: /Admin/Region/Create
        public IActionResult Create() => View(new RegionDto());

        // POST: /Admin/Region/Create
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(RegionDto dto)
        {
            if (!ModelState.IsValid)
                return View(dto);

            // 1) Lấy tất cả IDs hiện có
            var snap = await _db.Collection(COLL).GetSnapshotAsync();
            var maxNum = snap.Documents
                              .Select(d => d.Id)
                              .Where(id => id.StartsWith("region"))
                              .Select(id =>
                              {
                                  var tail = id.Substring("region".Length);
                                  return int.TryParse(tail, out var n) ? n : 0;
                              })
                              .DefaultIfEmpty(0)
                              .Max();

            // 2) Tính ID mới
            var newId = $"region{maxNum + 1:00}";

            // 3) Tạo document với ID mới
            await _db.Collection(COLL)
                     .Document(newId)
                     .SetAsync(dto);

            return RedirectToAction(nameof(Index));
        }

        // GET: /Admin/Region/Edit/{id}
        public async Task<IActionResult> Edit(string id)
        {
            if (string.IsNullOrEmpty(id)) return NotFound();

            var snap = await _db.Collection(COLL).Document(id).GetSnapshotAsync();
            if (!snap.Exists) return NotFound();

            var dto = snap.ConvertTo<RegionDto>();
            dto.Id = snap.Id;
            return View(dto);
        }

        // POST: /Admin/Region/Edit
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(RegionDto dto)
        {
            if (!ModelState.IsValid) return View(dto);

            await _db.Collection(COLL)
                     .Document(dto.Id)
                     .UpdateAsync("name", dto.Name);

            return RedirectToAction(nameof(Index));
        }

        // POST: /Admin/Region/Delete/{id}
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(string id)
        {
            if (string.IsNullOrEmpty(id)) return BadRequest();

            await _db.Collection(COLL)
                     .Document(id)
                     .DeleteAsync();

            return RedirectToAction(nameof(Index));
        }
    }
}
