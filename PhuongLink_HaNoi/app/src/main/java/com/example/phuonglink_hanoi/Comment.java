package com.example.phuonglink_hanoi;

import com.google.firebase.Timestamp;

public class Comment {
    private String id;
    private String postId;
    private String authorUserId; // Maps to 'authorUserId' in Firestore
    private String authorName; //

    private String content;
    private Timestamp createdAt; // Maps to 'createdAt' in Firestore

    public Comment() {
        // Public no-arg constructor needed for Firestore
    }

    // Constructor đã chỉnh sửa để khớp với các trường mới
    public Comment(String postId, String authorUserId, String authorName, String content, Timestamp createdAt) {
        this.postId = postId;
        this.authorUserId = authorUserId;
        this.authorName = authorName; // <--- Cập nhật
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getAuthorUserId() {
        return authorUserId;
    }

    public void setAuthorUserId(String authorUserId) {
        this.authorUserId = authorUserId;
    }

    // <--- CẬP NHẬT GETTER VÀ SETTER CHO authorName
    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}