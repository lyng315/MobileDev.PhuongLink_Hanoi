using System.Collections.Generic;
using System.Security.Claims;
using System.Text.Json;
using System.Net.Http.Json;
using System.Threading.Tasks;
using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.Extensions.Options;
using WebApplication1.Models.Dtos;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    [Authorize(Roles = "ADMIN")]
    [Route("Admin/[controller]/[action]")]
    public class ProfileController : Controller
    {
        private readonly FirestoreDb _firestore;
        private readonly HttpClient _httpClient;
        private readonly string _apiKey;

        public ProfileController(
            FirestoreDb firestore,
            IHttpClientFactory httpFactory,
            IOptions<FirebaseSettings> opts)
        {
            _firestore = firestore;
            _httpClient = httpFactory.CreateClient();
            _apiKey = opts.Value.ApiKey;
        }

        // GET: /Admin/Profile/Index
        [HttpGet]
        public async Task<IActionResult> Index()
        {
            var uid = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (string.IsNullOrEmpty(uid))
                return RedirectToAction("Login", "Account");

            var snap = await _firestore
                .Collection("users")
                .Document(uid)
                .GetSnapshotAsync();
            if (!snap.Exists)
                return RedirectToAction("Login", "Account");

            var dto = new ProfileDto
            {
                Email = snap.GetValue<string>("email"),
                FullName = snap.ContainsField("fullName") ? snap.GetValue<string>("fullName") : string.Empty,
                PhoneNumber = snap.ContainsField("phoneNumber") ? snap.GetValue<string>("phoneNumber") : string.Empty,
                Address = snap.ContainsField("address") ? snap.GetValue<string>("address") : string.Empty
            };

            var roleId = snap.GetValue<string>("roleId");
            var roleSnap = await _firestore
                .Collection("roles")
                .Document(roleId)
                .GetSnapshotAsync();
            dto.RoleName = roleSnap.Exists
                ? roleSnap.GetValue<string>("name")
                : "Unknown";

            return View(dto);
        }

        // POST: /Admin/Profile/Index
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Index(ProfileDto model)
        {
            var uid = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (string.IsNullOrEmpty(uid))
                return RedirectToAction("Login", "Account");

            var updates = new Dictionary<string, object>
            {
                ["fullName"] = model.FullName,
                ["phoneNumber"] = model.PhoneNumber,
                ["address"] = model.Address
            };

            await _firestore
                .Collection("users")
                .Document(uid)
                .UpdateAsync(updates);

            ViewBag.Message = "Cập nhật thành công";
            return View(model);
        }

        // GET: /Admin/Profile/ChangePassword
        [HttpGet("ChangePassword")]
        public IActionResult ChangePassword()
            => View(new ChangePasswordDto());

        // POST: /Admin/Profile/ChangePassword
        [HttpPost("ChangePassword")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> ChangePassword(ChangePasswordDto model)
        {
            if (!ModelState.IsValid)
                return View(model);

            var email = User.FindFirstValue(ClaimTypes.Email);
            if (string.IsNullOrEmpty(email))
                return RedirectToAction("Login", "Account");

            // Reauthenticate
            var signInUri = $"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={_apiKey}";
            var signInPayload = new { email, password = model.CurrentPassword, returnSecureToken = true };
            var signInResp = await _httpClient.PostAsJsonAsync(signInUri, signInPayload);
            if (!signInResp.IsSuccessStatusCode)
            {
                ModelState.AddModelError("CurrentPassword", "Mật khẩu cũ không đúng");
                return View(model);
            }

            var signInData = JsonSerializer.Deserialize<FirebaseAuthResponse>(
                await signInResp.Content.ReadAsStringAsync(),
                new JsonSerializerOptions { PropertyNameCaseInsensitive = true })!;

            // Update password
            var updateUri = $"https://identitytoolkit.googleapis.com/v1/accounts:update?key={_apiKey}";
            var updatePayload = new
            {
                idToken = signInData.idToken,
                password = model.NewPassword,
                returnSecureToken = true
            };
            var updateResp = await _httpClient.PostAsJsonAsync(updateUri, updatePayload);
            if (!updateResp.IsSuccessStatusCode)
            {
                var err = JsonSerializer.Deserialize<FirebaseErrorResponse>(
                    await updateResp.Content.ReadAsStringAsync(),
                    new JsonSerializerOptions { PropertyNameCaseInsensitive = true });
                ModelState.AddModelError("", err?.error?.message ?? "Lỗi khi đổi mật khẩu");
                return View(model);
            }

            var updateData = JsonSerializer.Deserialize<FirebaseAuthResponse>(
                await updateResp.Content.ReadAsStringAsync(),
                new JsonSerializerOptions { PropertyNameCaseInsensitive = true })!;

            var identity = (ClaimsIdentity)User.Identity!;
            var oldToken = identity.FindFirst("FirebaseToken");
            if (oldToken != null) identity.RemoveClaim(oldToken);
            identity.AddClaim(new Claim("FirebaseToken", updateData.idToken));

            ViewBag.Message = "Đổi mật khẩu thành công";
            return View(new ChangePasswordDto());
        }

        // POST: /Admin/Profile/Logout
        [HttpPost("Logout")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Logout()
        {
            await HttpContext.SignOutAsync(CookieAuthenticationDefaults.AuthenticationScheme);
            return RedirectToAction("Login", "Account");
        }

        private class FirebaseAuthResponse { public string idToken { get; set; } = string.Empty; }
        private class FirebaseErrorResponse { public FirebaseError error { get; set; } = null!; }
        private class FirebaseError { public string message { get; set; } = string.Empty; }
    }
}
