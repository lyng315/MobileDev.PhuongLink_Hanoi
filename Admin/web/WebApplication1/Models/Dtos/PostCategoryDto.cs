using Google.Cloud.Firestore;

namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class PostCategoryDto
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = "";

        // FirestoreSeeder dùng key "name" (lowercase)
        [FirestoreProperty("name")]
        public string Name { get; set; } = "";
    }
}
