using Google.Cloud.Firestore;


namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class UserDto
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = "";

        [FirestoreProperty("email")]
        public string Email { get; set; } = "";

        [FirestoreProperty("passwordHash")]
        public string PasswordHash { get; set; } = "";

        [FirestoreProperty("fullName")]
        public string FullName { get; set; } = "";

        [FirestoreProperty("phoneNumber")]
        public string PhoneNumber { get; set; } = "";

        [FirestoreProperty("regionId")]
        public string RegionId { get; set; } = "";

        [FirestoreProperty("addressDetail")]
        public string AddressDetail { get; set; } = "";

        [FirestoreProperty("isVerifiedResident")]
        public bool IsVerifiedResident { get; set; }

        [FirestoreProperty("roleId")]
        public string RoleId { get; set; } = "";

        [FirestoreProperty("cccd")]
        public string CCCD { get; set; } = "";

        [FirestoreProperty("createdAt")]
        public Timestamp CreatedAt { get; set; }

        public string RoleName { get; set; } = "";  // Không cần FirestoreIgnore
        public string RegionName { get; set; } = "";

    }
}
