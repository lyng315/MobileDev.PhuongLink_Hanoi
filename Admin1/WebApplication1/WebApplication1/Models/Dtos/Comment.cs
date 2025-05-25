// Models/Dtos/Comment.cs
using System;
using Google.Cloud.Firestore;

namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class Comment
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = string.Empty;

        [FirestoreProperty("content")]
        public string Content { get; set; } = string.Empty;

        [FirestoreProperty("createdAt")]
        public Timestamp CreatedAtTimestamp { get; set; }

        // Property chỉ có getter, Firestore sẽ tự ignore
        public DateTime CreatedAt => CreatedAtTimestamp.ToDateTime();

        // Firestore lưu DocumentReference
        [FirestoreProperty("postid")]
        public DocumentReference PostReference { get; set; } = default!;

        // Dùng trong View để hiển thị
        public string PostId => PostReference.Id;

        [FirestoreProperty("userid")]
        public DocumentReference UserReference { get; set; } = default!;

        public string UserId => UserReference.Id;
    }
}
