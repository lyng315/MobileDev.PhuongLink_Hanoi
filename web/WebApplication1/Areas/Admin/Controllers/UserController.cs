using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using System.Collections.Generic;
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
            // 1) Lấy snapshot
            var userSnap = await _db.Collection(COLL).GetSnapshotAsync();
            var regionSnap = await _db.Collection("regions").GetSnapshotAsync();
            var roleSnap = await _db.Collection("roles").GetSnapshotAsync();

            // 2) Chuẩn bị dropdown list roles (chỉ USER & LEADER)
            var allRoles = roleSnap.Documents
                                   .Where(d => d.Exists)
                                   .Select(d => d.ConvertTo<RoleDto>())
                                   .ToList();
            var allowedNames = new[] { "RESIDENT", "LEADER" };
            ViewBag.RolesList = allRoles
                .Where(r => allowedNames.Contains(r.RoleName))
                .ToList();

            // 3) Map regionId -> regionName
            var regionMap = regionSnap.Documents
                .Where(d => d.Exists)
                .ToDictionary(d => d.Id,
                              d => d.ConvertTo<RegionDto>().Name);

            // 4) Map roleId -> roleName
            var roleMap = allRoles
                .ToDictionary(r => r.Id, r => r.RoleName);

            // 5) Build list UserDto kèm RegionName & RoleName
            var list = userSnap.Documents
                .Select(d =>
                {
                    var dto = d.ConvertTo<UserDto>();
                    dto.Id = d.Id;
                    if (!string.IsNullOrEmpty(dto.RegionId) && regionMap.TryGetValue(dto.RegionId, out var rn))
                        dto.RegionName = rn;
                    if (!string.IsNullOrEmpty(dto.RoleId) && roleMap.TryGetValue(dto.RoleId, out var ro))
                        dto.RoleName = ro;
                    return dto;
                })
                // 6) Lọc bỏ ADMIN
                .Where(u => u.RoleName != "ADMIN")
                .ToList();

            return View(list);
        }

        // POST: /Admin/User/ChangeRole
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> ChangeRole(string id, string roleId)
        {
            if (string.IsNullOrEmpty(id) || string.IsNullOrEmpty(roleId))
                return BadRequest();

            await _db.Collection(COLL)
                     .Document(id)
                     .UpdateAsync(new Dictionary<string, object>
                     {
                         ["roleId"] = roleId
                     });

            return RedirectToAction(nameof(Index));
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

            var snap = await _db.Collection(COLL).GetSnapshotAsync();
            var maxNum = snap.Documents
                              .Select(d => d.Id)
                              .Where(id => id.StartsWith("user"))
                              .Select(id => int.TryParse(id.Substring(4), out var n) ? n : 0)
                              .DefaultIfEmpty(0)
                              .Max();
            var newId = $"user{maxNum + 1:00}";

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
