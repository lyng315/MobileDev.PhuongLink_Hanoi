using System.Text.Json;
using System.Net.Http.Json;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;

namespace WebApplication1.Controllers
{
    public class ForgotPasswordController : Controller
    {
        private readonly HttpClient _httpClient;
        private readonly string _apiKey;

        public ForgotPasswordController(
            IHttpClientFactory httpFactory,
            IOptions<FirebaseSettings> opts)
        {
            _httpClient = httpFactory.CreateClient();
            _apiKey = opts.Value.ApiKey;
        }

        // GET: /Account/ForgotPassword
        [HttpGet("Account/ForgotPassword")]
        public IActionResult ForgotPassword()
            => View("~/Views/Account/ForgotPassword.cshtml");

        // POST: /Account/ForgotPassword
        [HttpPost("Account/ForgotPassword")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> ForgotPassword(string email)
        {
            var uri = $"https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key={_apiKey}";
            var payload = new { requestType = "PASSWORD_RESET", email };
            var resp = await _httpClient.PostAsJsonAsync(uri, payload);

            // Đọc nguyên JSON để debug hoặc show message gốc
            var raw = await resp.Content.ReadAsStringAsync();

            if (!resp.IsSuccessStatusCode)
            {
                FirebaseErrorResponse ferr = null!;
                try
                {
                    ferr = JsonSerializer.Deserialize<FirebaseErrorResponse>(raw,
                        new JsonSerializerOptions { PropertyNameCaseInsensitive = true })!;
                }
                catch { /* ignore parse errors */ }

                var code = ferr?.error?.message;
                ViewBag.Error = code switch
                {
                    "EMAIL_NOT_FOUND" => "Email này chưa được đăng ký",
                    "INVALID_EMAIL" => "Định dạng email không hợp lệ",
                    "USER_DISABLED" => "Tài khoản đã bị vô hiệu hóa",
                    "OPERATION_NOT_ALLOWED" => "Chức năng đặt lại mật khẩu chưa được bật",
                    _ => raw
                };
            }
            else
            {
                ViewBag.Message = "Link đặt lại mật khẩu đã được gửi đến email của bạn.";
            }

            return View("~/Views/Account/ForgotPassword.cshtml");
        }

        // Model để map JSON lỗi từ Firebase
        private class FirebaseErrorResponse
        {
            public FirebaseError error { get; set; } = null!;
        }
        private class FirebaseError
        {
            public string message { get; set; } = "";
        }
    }
}
