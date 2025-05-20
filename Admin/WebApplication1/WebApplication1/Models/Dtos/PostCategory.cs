using Google.Cloud.Firestore;

namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class PostCategoryDto
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = "";

        [FirestoreProperty("Name")]
        public string Name { get; set; } = "";
    }
}