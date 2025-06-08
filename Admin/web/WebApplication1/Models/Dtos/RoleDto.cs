using System.Collections.Generic;
using Google.Cloud.Firestore;

namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class RoleDto
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = "";

        [FirestoreProperty("roleCode")]
        public string RoleCode { get; set; } = "";

        [FirestoreProperty("roleName")]
        public string RoleName { get; set; } = "";

        [FirestoreProperty("permissions")]
        public List<string> Permissions { get; set; } = new List<string>();
    }
}
