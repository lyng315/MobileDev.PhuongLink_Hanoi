using System;
using System.IO;
using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

var builder = WebApplication.CreateBuilder(args);

// 1) Bind “Firebase” section (ApiKey) từ appsettings.json
builder.Services.Configure<FirebaseSettings>(
    builder.Configuration.GetSection("Firebase")
);

// 2) Đăng ký IHttpClientFactory cho AccountController và ForgotPasswordController
builder.Services.AddHttpClient();

// 3) Cấu hình FirestoreDb
var fsSection = builder.Configuration.GetSection("Firestore");
var projectId = fsSection["ProjectId"]!;
var jsonKeyPath = Path.Combine(
    builder.Environment.ContentRootPath,
    fsSection["JsonKeyPath"]!  // Ví dụ: "Secrets/phuonglinkhanoi-firebase-adminsdk.json"
);
Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", jsonKeyPath);
builder.Services.AddSingleton(_ => FirestoreDb.Create(projectId));

// 4) Cấu hình cookie-based authentication
builder.Services
    .AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(options =>
    {
        options.LoginPath = "/Account/Login";
        options.AccessDeniedPath = "/Account/Login";
        // Có thể cấu hình thêm options.ExpireTimeSpan, options.Cookie.Name…
    });

// 5) Thêm MVC controllers với views
builder.Services.AddControllersWithViews();

var app = builder.Build();

// 6) Middleware pipeline
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();

app.UseAuthentication();
app.UseAuthorization();

// 7) Kích hoạt attribute routing (để các [HttpGet("Account/ForgotPassword")] hoạt động)
app.MapControllers();

// 8) Định nghĩa route cho Admin area
app.MapAreaControllerRoute(
    name: "admin",
    areaName: "Admin",
    pattern: "Admin/{controller=Dashboard}/{action=Index}/{id?}"
);

// 9) Route mặc định cho Account/Login
app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Account}/{action=Login}/{id?}"
);

app.Run();


// --- POCO để bind phần “Firebase” trong appsettings.json ---
public class FirebaseSettings
{
    public string ApiKey { get; set; } = "";
}
