package com.example.phuonglink_hanoi;

import com.google.firebase.Timestamp;

public class Comment {
    private String id;
    private String postId;
    private String authorUserId;
    private String authorName;
    private String authorAvatarUrl;
    private String content;
    private Timestamp createdAt;

    public Comment() {
        // No-arg constructor needed for Firestore
    }

    public Comment(String postId, String authorUserId, String authorName, String content, Timestamp createdAt) {
        this.postId = postId;
        this.authorUserId = authorUserId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    // ID
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // postId
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    // authorUserId
    public String getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(String authorUserId) { this.authorUserId = authorUserId; }

    // authorName
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    // authorAvatarUrl
    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public void setAuthorAvatarUrl(String authorAvatarUrl) { this.authorAvatarUrl = authorAvatarUrl; }

    // content
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    // createdAt
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
