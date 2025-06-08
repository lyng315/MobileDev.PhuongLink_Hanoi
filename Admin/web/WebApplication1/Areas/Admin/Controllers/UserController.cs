using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using System;
using System.Linq;
using System.Threading.Tasks;
using WebApplication1.Models.Dtos;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class UserController : Controller
    {
        private readonly FirestoreDb _db;
        private const string COLL = "users";

        public UserController(FirestoreDb db)
        {
            _db = db;
        }

        // GET: /Admin/User
        public async Task<IActionResult> Index()
        {
            var snap = await _db.Collection(COLL).GetSnapshotAsync();
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

        // GET: /Admin/User/Create
        public async Task<IActionResult> Create()
        {
            ViewBag.Regions = (await _db.Collection("regions").GetSnapshotAsync())
                              .Documents.Select(d => d.ConvertTo<RegionDto>())
                              .ToList();
            ViewBag.Roles = (await _db.Collection("roles").GetSnapshotAsync())
                              .Documents.Select(d => d.ConvertTo<RoleDto>())
                              .ToList();

            return View(new UserDto());
        }

        // POST: /Admin/User/Create
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(UserDto dto)
        {
            // Nếu validation lỗi, phải re-populate ViewBag trước khi trả View
            if (!ModelState.IsValid)
            {
                ViewBag.Regions = (await _db.Collection("regions").GetSnapshotAsync())
                                  .Documents.Select(d => d.ConvertTo<RegionDto>())
                                  .ToList();
                ViewBag.Roles = (await _db.Collection("roles").GetSnapshotAsync())
                                  .Documents.Select(d => d.ConvertTo<RoleDto>())
                                  .ToList();
                return View(dto);
            }

            // 1) Lấy các userId hiện có
            var snap = await _db.Collection(COLL).GetSnapshotAsync();
            var maxNum = snap.Documents
                              .Select(d => d.Id)
                              .Where(id => id.StartsWith("user"))
                              .Select(id =>
                              {
                                  var tail = id.Substring("user".Length);
                                  return int.TryParse(tail, out var n) ? n : 0;
                              })
                              .DefaultIfEmpty(0)
                              .Max();

            // 2) Tính ID mới
            var newId = $"user{maxNum + 1:00}";

            // 3) Tạo document với ID mới
            await _db.Collection(COLL)
                     .Document(newId)
                     .SetAsync(dto);

            return RedirectToAction(nameof(Index));
        }

        // GET: /Admin/User/Edit/{id}
        public async Task<IActionResult> Edit(string id)
        {
            if (string.IsNullOrEmpty(id)) return NotFound();

            var snap = await _db.Collection(COLL).Document(id).GetSnapshotAsync();
            if (!snap.Exists) return NotFound();

            var dto = snap.ConvertTo<UserDto>();
            dto.Id = snap.Id;

            ViewBag.Regions = (await _db.Collection("regions").GetSnapshotAsync())
                              .Documents.Select(d => d.ConvertTo<RegionDto>())
                              .ToList();
            ViewBag.Roles = (await _db.Collection("roles").GetSnapshotAsync())
                              .Documents.Select(d => d.ConvertTo<RoleDto>())
                              .ToList();

            return View(dto);
        }

        // POST: /Admin/User/Edit
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(UserDto dto)
        {
            if (!ModelState.IsValid)
            {
                ViewBag.Regions = (await _db.Collection("regions").GetSnapshotAsync())
                                  .Documents.Select(d => d.ConvertTo<RegionDto>())
                                  .ToList();
                ViewBag.Roles = (await _db.Collection("roles").GetSnapshotAsync())
                                  .Documents.Select(d => d.ConvertTo<RoleDto>())
                                  .ToList();
                return View(dto);
            }

            await _db.Collection(COLL)
                     .Document(dto.Id)
                     .UpdateAsync(new Dictionary<string, object>
                     {
                         ["email"] = dto.Email,
                         ["passwordHash"] = dto.PasswordHash,
                         ["fullName"] = dto.FullName,
                         ["phoneNumber"] = dto.PhoneNumber,
                         ["regionId"] = dto.RegionId,
                         ["addressDetail"] = dto.AddressDetail,
                         ["isVerifiedResident"] = dto.IsVerifiedResident,
                         ["roleId"] = dto.RoleId
                     });

            return RedirectToAction(nameof(Index));
        }

        // POST: /Admin/User/Delete/{id}
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
