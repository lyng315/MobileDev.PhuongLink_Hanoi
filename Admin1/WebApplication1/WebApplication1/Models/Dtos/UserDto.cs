using Google.Cloud.Firestore;

namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class UserDto
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = "";

        [FirestoreProperty("FullName")]
        public string FullName { get; set; } = "";

        [FirestoreProperty("Email")]
        public string Email { get; set; } = "";

        [FirestoreProperty("PhoneNumber")]
        public string PhoneNumber { get; set; } = "";

        [FirestoreProperty("CCCD")]
        public string CCCD { get; set; } = "";

        [FirestoreProperty("Role")]
        public string Role { get; set; } = "";

        [FirestoreProperty("Password")]
        public string Password { get; set; } = "";

        [FirestoreProperty("District")]
        public string District { get; set; } = "";

        [FirestoreProperty("Ward")]
        public string Ward { get; set; } = "";

        [FirestoreProperty("CreatedAt")]
        public Timestamp CreatedAt { get; set; }
    }
}
