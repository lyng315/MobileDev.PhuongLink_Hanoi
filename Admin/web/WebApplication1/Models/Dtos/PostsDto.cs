using Google.Cloud.Firestore;
using System;

namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class PostsDTO
    {
        public string Id { get; set; }

        [FirestoreProperty("title")]
        public string Title { get; set; }

        [FirestoreProperty("content")]
        public string Content { get; set; }

        [FirestoreProperty("status")]
        public string Status { get; set; }

        [FirestoreProperty("urgencyLevel")]
        public int UrgencyLevel { get; set; }

        [FirestoreProperty("authorUserId")]
        public string AuthorUserId { get; set; }

        [FirestoreProperty("categoryId")]
        public string CategoryId { get; set; }

        [FirestoreProperty("targetRegionId")]
        public string TargetRegionId { get; set; }

        [FirestoreProperty("createdAt")]
        public Timestamp CreatedAt { get; set; }

        [FirestoreProperty("editedAt")]
        public Timestamp EditedAt { get; set; }

        // View-only fields
        public string AuthorName { get; set; }


        public string CategoryName { get; set; }


        public string RegionName { get; set; }
    }
}
