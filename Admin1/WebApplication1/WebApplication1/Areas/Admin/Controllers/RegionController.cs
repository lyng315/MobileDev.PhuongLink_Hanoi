using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using System.Linq;
using System.Threading.Tasks;
using WebApplication1.Models.Dtos;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class RegionController : Controller
    {
        private readonly FirestoreDb _db;
        public RegionController(FirestoreDb db) => _db = db;

        // GET: /Admin/Region
        public async Task<IActionResult> Index(string keyword)
        {
            var snap = await _db.Collection("Regions").GetSnapshotAsync();
            var list = snap.Documents
                .Select(d =>
                {
                    var dto = d.ConvertTo<RegionDto>();
                    dto.Id = d.Id;
                    return dto;
                })
                .ToList();

            if (!string.IsNullOrWhiteSpace(keyword))
            {
                list = list
                    .Where(r =>
                        r.Ward.Contains(keyword, StringComparison.OrdinalIgnoreCase)
                        || r.District.Contains(keyword, StringComparison.OrdinalIgnoreCase))
                    .ToList();
            }

            return View(list);
        }

        // GET: /Admin/Region/Create
        public IActionResult Create() => View(new RegionDto());

        // POST: /Admin/Region/Create
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(RegionDto dto)
        {
            if (!ModelState.IsValid) return View(dto);

            var doc = _db.Collection("Regions").Document();
            await doc.SetAsync(new { dto.Ward, dto.District });

            return RedirectToAction(nameof(Index));
        }

        // GET: /Admin/Region/Edit/{id}
        public async Task<IActionResult> Edit(string id)
        {
            var snap = await _db.Collection("Regions").Document(id).GetSnapshotAsync();
            if (!snap.Exists) return NotFound();

            var dto = snap.ConvertTo<RegionDto>();
            dto.Id = id;
            return View(dto);
        }

        // POST: /Admin/Region/Edit
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(RegionDto dto)
        {
            if (!ModelState.IsValid) return View(dto);

            var docRef = _db.Collection("Regions").Document(dto.Id);
            await docRef.UpdateAsync(new Dictionary<string, object>
            {
                { "Ward",     dto.Ward     },
                { "District", dto.District }
            });

            return RedirectToAction(nameof(Index));
        }

        // POST: /Admin/Region/Delete/{id}
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(string id)
        {
            await _db.Collection("Regions").Document(id).DeleteAsync();
            return RedirectToAction(nameof(Index));
        }
    }
}
