using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;
using System.Net.Http.Json;
using System.Security.Claims;

namespace WebApplication1.Controllers
{
    public class AccountController : Controller
    {
        private readonly HttpClient _httpClient;
        private readonly FirestoreDb _firestore;
        private readonly string _apiKey;

        public AccountController(
            IHttpClientFactory httpClientFactory,
            FirestoreDb firestore,
            IOptions<FirebaseSettings> opts)
        {
            _httpClient = httpClientFactory.CreateClient();
            _firestore = firestore;
            _apiKey = opts.Value.ApiKey;
        }

        [HttpGet]
        [Route("/")]
        [Route("Account/Login")]
        public IActionResult Login()
            => View();

        [HttpPost]
        [Route("Account/Login")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Login(string username, string password)
        {
            // 1) Đăng nhập qua Firebase Auth REST API
            var uri = $"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={_apiKey}";
            var payload = new
            {
                email = username,
                password = password,
                returnSecureToken = true
            };
            var resp = await _httpClient.PostAsJsonAsync(uri, payload);
            if (!resp.IsSuccessStatusCode)
            {
                ViewBag.Error = "Email hoặc mật khẩu không đúng";
                return View();
            }

            var auth = await resp.Content.ReadFromJsonAsync<FirebaseAuthResponse>();
            var uid = auth!.localId;

            // 2) Lấy thông tin user từ Firestore (collection "users")
            var userDoc = await _firestore
                .Collection("users")
                .Document(uid)
                .GetSnapshotAsync();
            if (!userDoc.Exists)
            {
                ViewBag.Error = "Không tìm thấy hồ sơ người dùng";
                return View();
            }

            var roleId = userDoc.GetValue<string>("roleId");

            // 3) Lấy document role tương ứng từ collection "roles"
            var roleDoc = await _firestore
                .Collection("roles")
                .Document(roleId)
                .GetSnapshotAsync();
            if (!roleDoc.Exists)
            {
                ViewBag.Error = "Không tìm thấy thông tin quyền hạn";
                return View();
            }

            var roleName = roleDoc.GetValue<string>("name");
            if (!roleName.Equals("ADMIN", StringComparison.OrdinalIgnoreCase))
            {
                ViewBag.Error = "Bạn không có quyền truy cập trang quản trị";
                return View();
            }

            // 4) Tạo Cookie Authentication cho phiên làm việc
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.NameIdentifier, uid),
                new Claim(ClaimTypes.Email, username),
                new Claim(ClaimTypes.Role, roleName)
            };
            var identity = new ClaimsIdentity(claims, CookieAuthenticationDefaults.AuthenticationScheme);
            await HttpContext.SignInAsync(
                CookieAuthenticationDefaults.AuthenticationScheme,
                new ClaimsPrincipal(identity)
            );

            // 5) Chuyển hướng vào Admin Dashboard
            return RedirectToAction("Index", "Dashboard", new { area = "Admin" });
        }

        // POCO cho JSON trả về khi login thành công
        private class FirebaseAuthResponse
        {
            public string localId { get; set; } = "";
            public string idToken { get; set; } = "";
            public string email { get; set; } = "";
        }
    }
}
