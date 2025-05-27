using Google.Cloud.Firestore;

namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class RegionDto
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = "";

        // FirestoreSeeder lưu vào field "name"
        [FirestoreProperty("name")]
        public string Name { get; set; } = "";
    }
}
