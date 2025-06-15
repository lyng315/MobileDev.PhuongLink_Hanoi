package com.example.phuonglink_hanoi;

import com.google.firebase.Timestamp;

public class Post {
    private String id;
    private String title;
    private int urgencyLevel;
    private Timestamp createdAt;
    private String thumbnailUrl;

    public Post() { }

    /**
     * Constructor gốc của bạn, bao gồm ID.
     */
    public Post(String id, String title, int urgencyLevel, Timestamp createdAt, String thumbnailUrl) {
        this.id           = id;
        this.title        = title;
        this.urgencyLevel = urgencyLevel;
        this.createdAt    = createdAt;
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * NEW: Constructor không cần id, để khớp với new Post(4 tham số) ở call site.
     * Bạn có thể gán id mặc định hoặc để null tùy nhu cầu.
     */
    public Post(String title, int urgencyLevel, Timestamp createdAt, String thumbnailUrl) {
        this.id           = null;            // hoặc "" hoặc UUID.randomUUID().toString()
        this.title        = title;
        this.urgencyLevel = urgencyLevel;
        this.createdAt    = createdAt;
        this.thumbnailUrl = thumbnailUrl;
    }

    // -- GETTERS & SETTERS --
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(int urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
}
