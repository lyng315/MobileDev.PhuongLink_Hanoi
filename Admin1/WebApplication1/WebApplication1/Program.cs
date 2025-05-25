using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using System.IO;

var builder = WebApplication.CreateBuilder(args);

// --- Cấu hình Firestore ---
var fs = builder.Configuration.GetSection("Firestore");
var projectId = fs["ProjectId"]!;
var jsonKeyPath = Path.Combine(
    builder.Environment.ContentRootPath,
    fs["JsonKeyPath"]!
);

// cho Google SDK biết file credentials
Environment.SetEnvironmentVariable(
    "GOOGLE_APPLICATION_CREDENTIALS",
    jsonKeyPath
);

// đăng ký FirestoreDb để inject
builder.Services.AddSingleton(_ => FirestoreDb.Create(projectId));

// --- Thêm MVC ---
builder.Services.AddControllersWithViews();

var app = builder.Build();

// middleware
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}
app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();
app.UseAuthorization();

// --- Route cho tất cả các Area ---
app.MapAreaControllerRoute(
    name: "admin",
    areaName: "Admin",
    pattern: "Admin/{controller=Dashboard}/{action=Index}/{id?}"
);

// --- Route mặc định cho non-Area ---
app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}"
);

app.Run();
