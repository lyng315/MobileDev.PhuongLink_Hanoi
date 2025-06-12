using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using System;
using System.IO;
using Google.Cloud.Firestore;
//using WebApplication1.Services;

var builder = WebApplication.CreateBuilder(args);

// --- Cấu hình Firestore ---
var fs = builder.Configuration.GetSection("Firestore");
var projectId = fs["ProjectId"]!;

// Đường dẫn relative tới file JSON trong thư mục Secrets
var jsonKeyPath = Path.Combine(
    builder.Environment.ContentRootPath,
    fs["JsonKeyPath"]!    // sẽ là "Secrets/phuonglinkhanoi-firebase-adminsdk-fbsvc-f772f71851.json"
);

// Cho Google SDK biết credentials
Environment.SetEnvironmentVariable(
    "GOOGLE_APPLICATION_CREDENTIALS",
    jsonKeyPath
);

// Đăng ký FirestoreDb và Seeder
builder.Services.AddSingleton(_ => FirestoreDb.Create(projectId));
//builder.Services.AddTransient<FirestoreSeeder>();

// --- Cấu hình MVC/Web ---
builder.Services.AddControllersWithViews();

var app = builder.Build();

// --- Chạy seed ngay khi start ---
//using (var scope = app.Services.CreateScope())
//{
//    var seeder = scope.ServiceProvider.GetRequiredService<FirestoreSeeder>();
//    await seeder.SeedAsync();
//}

// --- Middleware & Routing ---
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();
app.UseAuthorization();

app.MapAreaControllerRoute(
    name: "admin",
    areaName: "Admin",
    pattern: "Admin/{controller=Dashboard}/{action=Index}/{id?}"
);

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}"
);

app.Run();
