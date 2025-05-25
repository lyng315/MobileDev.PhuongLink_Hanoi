// Areas/Admin/Controllers/CommentController.cs
using System.Linq;
using System.Threading.Tasks;
using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using WebApplication1.Models.Dtos;

namespace WebApplication1.Areas.Admin.Controllers
{
    [Area("Admin")]
    public class CommentController : Controller
    {
        private readonly FirestoreDb _db;
        private const string COLL = "Comment";

        public CommentController(FirestoreDb db) => _db = db;

        // GET: /Admin/Comment
        public async Task<IActionResult> Index()
        {
            var snap = await _db
                .Collection(COLL)
                .OrderBy("createdAt")
                .GetSnapshotAsync();

            var list = snap.Documents
                .Select(d => new Comment
                {
                    Id = d.Id,
                    Content = d.GetValue<string>("content"),
                    CreatedAtTimestamp = d.GetValue<Timestamp>("createdAt"),
                    PostReference = d.GetValue<DocumentReference>("postid"),
                    UserReference = d.GetValue<DocumentReference>("userid")
                })
                .ToList();

            return View(list);
        }

        // POST: /Admin/Comment/Delete/{id}
        [HttpPost, ValidateAntiForgeryToken]
        public async Task<IActionResult> Delete(string id)
        {
            await _db
                .Collection(COLL)
                .Document(id)
                .DeleteAsync();

            return RedirectToAction(nameof(Index));
        }
    }
}
