using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.Hosting;
using System.IO;

var builder = WebApplication.CreateBuilder(args);

// 0. Đọc config Firestore
var fsConfig = builder.Configuration.GetSection("Firestore");
string projectId = fsConfig["ProjectId"]!;
string jsonKeyPath = Path.Combine(builder.Environment.ContentRootPath, fsConfig["JsonKeyPath"]!);

// 1. Thiết lập biến môi trường cho SDK
Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", jsonKeyPath);

// 2. Đăng ký FirestoreDb
builder.Services.AddSingleton(_ => FirestoreDb.Create(projectId));

// 3. Đăng ký MVC
builder.Services.AddControllersWithViews();

var app = builder.Build();

// 4. Môi trường
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

// 5. Middleware
app.UseHttpsRedirection();
app.UseStaticFiles();
app.UseRouting();
app.UseAuthorization();

// 6. Area routing
app.MapControllerRoute(
    name: "areas",
    pattern: "{area:exists}/{controller=Dashboard}/{action=Index}/{id?}");

// 7. Default routing
app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");

app.Run();
