package com.example.phuonglink_hanoi;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.Exclude;

@IgnoreExtraProperties
public class Post {
    private String id;
    private String authorUserId;
    private String categoryId;
    private String title;
    private String content;
    private int urgencyLevel;
    private Timestamp createdAt;
    private Timestamp editedAt;
    private String status;
    private String targetRegionId;
    private String thumbnailUrl;

    // Constructor công khai bắt buộc để Firestore mapping
    public Post() { }

    // --- Các getter / setter khớp hoàn toàn với Firestore keys ---

    // id này do ta set thủ công sau khi toObject()
    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(String authorUserId) { this.authorUserId = authorUserId; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(int urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getEditedAt() { return editedAt; }
    public void setEditedAt(Timestamp editedAt) { this.editedAt = editedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTargetRegionId() { return targetRegionId; }
    public void setTargetRegionId(String targetRegionId) { this.targetRegionId = targetRegionId; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    // --- Các trường hỗ trợ hiển thị, không map lên Firestore ---
    @Exclude
    private String severity;
    @Exclude
    private String timeAgo;

    @Exclude
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    @Exclude
    public String getTimeAgo() { return timeAgo; }
    public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
}
