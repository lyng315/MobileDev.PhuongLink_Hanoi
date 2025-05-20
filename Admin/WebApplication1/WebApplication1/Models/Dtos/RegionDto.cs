using Google.Cloud.Firestore;

namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class RegionDto
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = "";

        [FirestoreProperty("Ward")]
        public string Ward { get; set; } = "";

        [FirestoreProperty("District")]
        public string District { get; set; } = "";
    }
}
