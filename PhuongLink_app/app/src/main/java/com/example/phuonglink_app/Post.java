package com.example.phuonglink_app;

import com.google.firebase.Timestamp;

/**
 * Model class ánh xạ dữ liệu từ Firestore collection "posts"
 */
public class Post {

    private String id;            // document ID (ví dụ "post01")
    private String title;         // trường "title" trong Firestore
    private int urgencyLevel;     // trường "urgencyLevel" (1, 2, 3...)
    private Timestamp createdAt;  // trường "createdAt" (Firestore timestamp)
    private String thumbnailUrl;  // trường "thumbnailUrl" (URL lên Cloud Storage)

    // Constructor mặc định bắt buộc cho Firestore
    public Post() { }

    // Constructor đầy đủ (có id)
    public Post(String id, String title, int urgencyLevel, Timestamp createdAt, String thumbnailUrl) {
        this.id = id;
        this.title = title;
        this.urgencyLevel = urgencyLevel;
        this.createdAt = createdAt;
        this.thumbnailUrl = thumbnailUrl;
    }

    // Getter / Setter
    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(int urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
