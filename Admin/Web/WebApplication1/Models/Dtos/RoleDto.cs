using System.Collections.Generic;
using Google.Cloud.Firestore;

namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class RoleDto
    {
        // Lấy ID của document
        [FirestoreDocumentId]
        public string Id { get; set; } = "";

        // Map field 'name' trong Firestore thành RoleName
        [FirestoreProperty("name")]
        public string RoleName { get; set; } = "";

        // Nếu sau này bạn thêm trường permissions vào Firestore, giữ mapping này
        [FirestoreProperty("permissions")]
        public List<string> Permissions { get; set; } = new List<string>();

        // Nếu có thêm field 'roleCode' ở Firestore, bỏ comment và map như sau:
        // [FirestoreProperty("roleCode")]
        // public string RoleCode { get; set; } = "";
    }
}
