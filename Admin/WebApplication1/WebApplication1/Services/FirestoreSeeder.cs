using Google.Cloud.Firestore;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace WebApplication1.Services
{
    public class FirestoreSeeder
    {
        private readonly FirestoreDb _db;
        public FirestoreSeeder(FirestoreDb db) => _db = db;

        public async Task SeedAsync()
        {
            await SeedRegions();
            await SeedRoles();
            await SeedUsers();
            await SeedPostCategories();
            await SeedPosts();
            await SeedComments();
            await SeedAttachments();
            await SeedNotificationHistory();
            Console.WriteLine("Seeding completed.");
        }

        private async Task SeedRegions()
        {
            var col = _db.Collection("regions");
            for (int i = 1; i <= 5; i++)
            {
                string id = $"region{i:00}";
                await col.Document(id).SetAsync(new Dictionary<string, object>
                {
                    { "name", $"Region {i}" }
                });
            }
        }

        private async Task SeedRoles()
        {
            var col = _db.Collection("roles");
            var roles = new[]
            {
                new { id = "role01", code = "USER",   name = "User",   permissions = new[] { "READ_POST", "COMMENT" } },
                new { id = "role02", code = "LEADER", name = "Leader", permissions = new[] { "READ_POST", "COMMENT", "CREATE_POST", "EDIT_POST", "DELETE_POST" } }
            };

            foreach (var r in roles)
            {
                await col.Document(r.id).SetAsync(new Dictionary<string, object>
                {
                    { "roleCode",    r.code },
                    { "roleName",    r.name },
                    { "permissions", r.permissions }
                });
            }
        }

        private async Task SeedUsers()
        {
            var col = _db.Collection("users");
            for (int i = 1; i <= 5; i++)
            {
                string id = $"user{i:00}";
                await col.Document(id).SetAsync(new Dictionary<string, object>
                {
                    { "email",             $"user{i}@example.com" },
                    { "passwordHash",      "HASHED_PASSWORD" },
                    { "fullName",          $"User {i}" },
                    { "phoneNumber",       $"01234567{i:00}" },
                    { "regionId",          $"region{i:00}" },
                    { "addressDetail",     $"Address {i}" },
                    { "isVerifiedResident", i % 2 == 0 },
                    { "roleId",            i % 2 == 0 ? "role02" : "role01" }
                });
            }
        }

        private async Task SeedPostCategories()
        {
            var col = _db.Collection("postCategories");
            var names = new[]
            {
                "Thông báo chung",
                "Cảnh báo an ninh",
                "Cảnh báo dịch vụ",
                "Sự kiện cộng đồng",
                "Thảo luận/góp ý"
            };
            for (int i = 0; i < names.Length; i++)
            {
                string id = $"category{i + 1:00}";
                await col.Document(id).SetAsync(new Dictionary<string, object>
                {
                    { "name", names[i] }
                });
            }
        }

        private async Task SeedPosts()
        {
            var col = _db.Collection("posts");
            var now = Timestamp.FromDateTime(DateTime.UtcNow);
            for (int i = 1; i <= 5; i++)
            {
                string id = $"post{i:00}";
                await col.Document(id).SetAsync(new Dictionary<string, object>
                {
                    { "authorUserId",   $"user{i:00}" },
                    { "categoryId",     $"category{i:00}" },
                    { "title",          $"Post Title {i}" },
                    { "content",        $"Sample content for post {i}" },
                    { "targetRegionId", $"region{i:00}" },
                    { "urgencyLevel",   i % 3 + 1 },
                    { "status",         i % 2 == 0 ? "Published" : "Draft" },
                    { "createdAt",      now },
                    { "editedAt",       now }
                });
            }
        }

        private async Task SeedComments()
        {
            var col = _db.Collection("comments");
            var now = Timestamp.FromDateTime(DateTime.UtcNow);
            for (int i = 1; i <= 5; i++)
            {
                string id = $"comment{i:00}";
                await col.Document(id).SetAsync(new Dictionary<string, object>
                {
                    { "postId",       $"post{i:00}" },
                    { "authorUserId", $"user{i:00}" },
                    { "content",      $"Sample comment {i}" },
                    { "createdAt",    now }
                });
            }
        }

        private async Task SeedAttachments()
        {
            var col = _db.Collection("attachments");
            var now = Timestamp.FromDateTime(DateTime.UtcNow);
            for (int i = 1; i <= 5; i++)
            {
                string id = $"attachment{i:00}";
                await col.Document(id).SetAsync(new Dictionary<string, object>
                {
                    { "filePath",   $"files/file{i}.pdf" },
                    { "fileType",   "application/pdf" },
                    { "uploadedAt", now }
                });
            }
        }

        private async Task SeedNotificationHistory()
        {
            var col = _db.Collection("notificationHistory");
            var now = Timestamp.FromDateTime(DateTime.UtcNow);
            for (int i = 1; i <= 5; i++)
            {
                string id = $"notification{i:00}";
                await col.Document(id).SetAsync(new Dictionary<string, object>
                {
                    { "notificationId", $"TB{i:00}" },
                    { "recipientId",    $"user{i:00}" },
                    { "postId",         $"post{i:00}" },
                    { "sentAt",         now },
                    { "status",         i % 2 == 0 ? "Sent" : "Failed" }
                });
            }
        }
    }
}
