using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using System.Linq;
using System.Threading.Tasks;
using WebApplication1.Models.Dtos;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class UserController : Controller
    {
        private readonly FirestoreDb _db;
        public UserController(FirestoreDb db) => _db = db;

        // GET: /Admin/User
        public async Task<IActionResult> Index()
        {
            var snap = await _db.Collection("users").GetSnapshotAsync();
            var list = snap.Documents
                .Select(d =>
                {
                    var dto = d.ConvertTo<UserDto>();
                    dto.Id = d.Id;
                    return dto;
                })
                .ToList();

            return View(list);
        }
        // Đây là GET Create
        public async Task<IActionResult> Create()
        {
            var regionsSnap = await _db.Collection("Regions").GetSnapshotAsync();
            var regions = regionsSnap.Documents.Select(d => d.ConvertTo<RegionDto>()).ToList();

            ViewBag.Districts = regions.Select(r => r.District).Distinct().OrderBy(d => d).ToList();
            ViewBag.Regions = regions;

            return View(new UserDto());
        }

        // POST: /Admin/User/Create
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(UserDto dto)
        {
            if (!ModelState.IsValid) return View(dto);

            var doc = _db.Collection("users").Document();
            dto.CreatedAt = Timestamp.FromDateTime(DateTime.UtcNow);
            await doc.SetAsync(dto);

            return RedirectToAction(nameof(Index));
        }

        // GET: /Admin/User/Edit/{id}
        public async Task<IActionResult> Edit(string id)
        {
            var snap = await _db.Collection("users").Document(id).GetSnapshotAsync();
            if (!snap.Exists) return NotFound();

            var dto = snap.ConvertTo<UserDto>();
            dto.Id = id;
            return View(dto);
        }

        // POST: /Admin/User/Edit
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(UserDto dto)
        {
            if (!ModelState.IsValid) return View(dto);

            var docRef = _db.Collection("users").Document(dto.Id);
            await docRef.UpdateAsync(new Dictionary<string, object>
            {
                { "FullName", dto.FullName },
                { "Email", dto.Email },
                { "PhoneNumber", dto.PhoneNumber },
                { "CCCD", dto.CCCD },
                { "Role", dto.Role },
                { "Password", dto.Password } // ❗ nếu bạn lưu mật khẩu thô
            });

            return RedirectToAction(nameof(Index));
        }

        // POST: /Admin/User/Delete/{id}
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(string id)
        {
            await _db.Collection("users").Document(id).DeleteAsync();
            return RedirectToAction(nameof(Index));
        }
    }
}
