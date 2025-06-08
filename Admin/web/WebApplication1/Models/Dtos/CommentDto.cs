using System;
using Google.Cloud.Firestore;

namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class CommentDto
    {
        [FirestoreDocumentId]
        public string Id { get; set; } = "";

        [FirestoreProperty("postId")]
        public string PostId { get; set; } = "";

        [FirestoreProperty("authorUserId")]
        public string AuthorUserId { get; set; } = "";

        [FirestoreProperty("content")]
        public string Content { get; set; } = "";

        [FirestoreProperty("createdAt")]
        public Timestamp CreatedAt { get; set; }

        public DateTime CreatedAtDate => CreatedAt.ToDateTime();

        // -- UI only, không map lên Firestore --
        public string PostTitle { get; set; } = "";
        public string AuthorEmail { get; set; } = "";
    }
}
