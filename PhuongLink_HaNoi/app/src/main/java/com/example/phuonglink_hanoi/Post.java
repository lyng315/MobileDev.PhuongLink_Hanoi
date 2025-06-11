package com.example.phuonglink_hanoi;

import com.google.firebase.Timestamp;

public class Post {
    private String id;
    private String title;
    private int urgencyLevel;
    private Timestamp createdAt;
    private String thumbnailUrl;
    private String targetRegionId;  // ← đổi tên cho khớp Firestore

    public Post() {}

    public Post(String id,
                String title,
                int urgencyLevel,
                Timestamp createdAt,
                String thumbnailUrl,
                String targetRegionId) {
        this.id = id;
        this.title = title;
        this.urgencyLevel = urgencyLevel;
        this.createdAt = createdAt;
        this.thumbnailUrl = thumbnailUrl;
        this.targetRegionId = targetRegionId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

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

    public String getTargetRegionId() {
        return targetRegionId;
    }
    public void setTargetRegionId(String targetRegionId) {
        this.targetRegionId = targetRegionId;
    }
}
