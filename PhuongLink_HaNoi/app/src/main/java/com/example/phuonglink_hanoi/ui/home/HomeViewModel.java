package com.example.phuonglink_hanoi.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Log;

import com.example.phuonglink_hanoi.Post;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HomeViewModel extends ViewModel {
    private static final String TAG = "HomeViewModel";
    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Post>> getPosts() {
        return posts;
    }

    /**
     * Lấy regionId từ user hiện tại và xử lý tiếp
     */
    private void getUserRegion(Consumer<String> onRegionReceived) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            posts.setValue(new ArrayList<>());
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String regionId = userDoc.getString("regionId");
                    if (regionId != null && !regionId.isEmpty()) {
                        onRegionReceived.accept(regionId);
                    } else {
                        posts.setValue(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user region", e);
                    posts.setValue(new ArrayList<>());
                });
    }

    /**
     * Load toàn bộ bài viết theo region của user, sắp xếp giảm dần theo createdAt
     */
    public void loadPostsByUserRegion() {
        getUserRegion(regionId ->
                db.collection("posts")
                        .whereEqualTo("targetRegionId", regionId)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener(this::mapSnapshotToPosts)
                        .addOnFailureListener(e -> Log.e(TAG, "Error loading posts", e))
        );
    }

    /**
     * Tìm kiếm bài viết theo title trong region của user
     */
    public void searchPostsByTitleInUserRegion(String keyword) {
        getUserRegion(regionId ->
                db.collection("posts")
                        .whereEqualTo("targetRegionId", regionId)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            List<Post> filtered = new ArrayList<>();
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Post p = doc.toObject(Post.class);
                                if (p != null) {
                                    p.setId(doc.getId());
                                    String title = p.getTitle();
                                    if (title != null && title.toLowerCase().contains(keyword.toLowerCase())) {
                                        filtered.add(p);
                                    }
                                }
                            }
                            posts.setValue(filtered);
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Error searching posts", e))
        );
    }

    /**
     * Lọc bài viết theo categoryId và urgencyLevel trong region của user
     */
    public void filterPostsByCategoryAndUrgency(String categoryId, String urgencyLevelStr) {
        getUserRegion(regionId ->
                db.collection("posts")
                        .whereEqualTo("targetRegionId", regionId)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            List<Post> filtered = new ArrayList<>();
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                Post p = doc.toObject(Post.class);
                                if (p != null) {
                                    p.setId(doc.getId());
                                    boolean matchesCategory = (categoryId == null || categoryId.isEmpty())
                                            || categoryId.equals(doc.getString("categoryId"));
                                    boolean matchesUrgency = true;
                                    if (urgencyLevelStr != null && !urgencyLevelStr.isEmpty()) {
                                        try {
                                            matchesUrgency = p.getUrgencyLevel() == Integer.parseInt(urgencyLevelStr);
                                        } catch (NumberFormatException e) {
                                            Log.e(TAG, "Invalid urgency level", e);
                                            matchesUrgency = false;
                                        }
                                    }
                                    if (matchesCategory && matchesUrgency) {
                                        filtered.add(p);
                                    }
                                }
                            }
                            // Sắp xếp giảm dần theo thời gian tạo
                            filtered.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                            posts.setValue(filtered);
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Error filtering posts", e))
        );
    }

    /**
     * Map QuerySnapshot thành danh sách Post và gắn id
     */
    private void mapSnapshotToPosts(QuerySnapshot snapshots) {
        List<Post> list = new ArrayList<>();
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            Post p = doc.toObject(Post.class);
            if (p != null) {
                p.setId(doc.getId());
                list.add(p);
            }
        }
        posts.setValue(list);
    }
}
