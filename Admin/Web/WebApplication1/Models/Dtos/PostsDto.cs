using Google.Cloud.Firestore;
using System;
using System.Collections.Generic;

namespace WebApplication1.Models.Dtos
{
    [FirestoreData]
    public class PostsDTO
    {
        // Document ID stored in Firestore
        [FirestoreDocumentId]
        public string Id { get; set; } = string.Empty;

        [FirestoreProperty("title")]
        public string Title { get; set; } = string.Empty;

        [FirestoreProperty("content")]
        public string Content { get; set; } = string.Empty;

        // Thumbnail URL riêng biệt
        [FirestoreProperty("thumbnailUrl")]
        public string ThumbnailUrl { get; set; } = string.Empty;

        // URLs of images associated with the post
        [FirestoreProperty("imageUrls")]
        public List<string> ImageUrls { get; set; } = new List<string>();

        [FirestoreProperty("urgencyLevel")]
        public int UrgencyLevel { get; set; }

        [FirestoreProperty("authorUserId")]
        public string AuthorUserId { get; set; } = string.Empty;

        [FirestoreProperty("categoryId")]
        public string CategoryId { get; set; } = string.Empty;

        [FirestoreProperty("targetRegionId")]
        public string TargetRegionId { get; set; } = string.Empty;

        [FirestoreProperty("createdAt")]
        public Timestamp CreatedAt { get; set; }

        [FirestoreProperty("editedAt")]
        public Timestamp EditedAt { get; set; }

        // Display-only fields, not stored in Firestore
        public string AuthorName { get; set; } = string.Empty;
        public string CategoryName { get; set; } = string.Empty;
        public string RegionName { get; set; } = string.Empty;
    }
}
