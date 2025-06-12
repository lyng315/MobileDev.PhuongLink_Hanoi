package com.example.phuonglink_hanoi.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Post>> getPosts() {
        return posts;
    }

    /** Lấy regionId từ user hiện tại */
    private void getUserRegion(Consumer<String> onRegionReceived) {
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
                    e.printStackTrace();
                    posts.setValue(new ArrayList<>());
                });
    }

    /** Load bài viết theo khu vực của user */
    public void loadPostsByUserRegion() {
        getUserRegion(regionId -> {
            db.collection("posts")
                    .whereEqualTo("targetRegionId", regionId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(this::mapSnapshotToPosts)
                    .addOnFailureListener(Throwable::printStackTrace);
        });
    }

    /** Tìm kiếm bài viết theo tiêu đề trong khu vực của user */
    public void searchPostsByTitleInUserRegion(String keyword) {
        getUserRegion(regionId -> {
            db.collection("posts")
                    .whereEqualTo("targetRegionId", regionId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<Post> filtered = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String title = doc.getString("title");
                            if (title != null && title.toLowerCase().contains(keyword.toLowerCase())) {
                                filtered.add(doc.toObject(Post.class));
                            }
                        }
                        posts.setValue(filtered);
                    })
                    .addOnFailureListener(Throwable::printStackTrace);
        });
    }

    /** Lọc bài viết theo categoryId và urgencyLevel trong khu vực của user */
    public void filterPostsByCategoryAndUrgency(String categoryId, String urgencyLevelStr) {
        getUserRegion(regionId -> {
            db.collection("posts")
                    .whereEqualTo("targetRegionId", regionId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        List<Post> filteredPosts = new ArrayList<>();

                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            String docCategoryId = doc.getString("categoryId");
                            Long urgencyLong = doc.getLong("urgencyLevel");
                            int urgency = urgencyLong != null ? urgencyLong.intValue() : -1;

                            boolean matchesCategory = (categoryId == null || categoryId.isEmpty()) || categoryId.equals(docCategoryId);
                            boolean matchesUrgency = true;

                            if (urgencyLevelStr != null && !urgencyLevelStr.isEmpty()) {
                                try {
                                    int urgencyLevel = Integer.parseInt(urgencyLevelStr);
                                    matchesUrgency = urgency == urgencyLevel;
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                    matchesUrgency = false;
                                }
                            }

                            if (matchesCategory && matchesUrgency) {
                                String id = doc.getId();
                                String title = doc.getString("title");
                                Timestamp createdAt = doc.getTimestamp("createdAt");
                                String thumbnailUrl = doc.getString("thumbnailUrl");
                                String targetRegion = doc.getString("targetRegionId");

                                filteredPosts.add(new Post(
                                        id,
                                        title != null ? title : "",
                                        urgency,
                                        createdAt != null ? createdAt : Timestamp.now(),
                                        thumbnailUrl != null ? thumbnailUrl : "",
                                        targetRegion != null ? targetRegion : ""
                                ));
                            }
                        }

                        // Sắp xếp theo thời gian giảm dần
                        filteredPosts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                        posts.setValue(filteredPosts);
                    })
                    .addOnFailureListener(Throwable::printStackTrace);
        });
    }


    /** Chuyển dữ liệu từ Firestore sang danh sách Post */
    private void mapSnapshotToPosts(QuerySnapshot snapshots) {
        List<Post> list = new ArrayList<>();
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            String id = doc.getId();
            String title = doc.getString("title");
            Long ul = doc.getLong("urgencyLevel");
            int urgency = ul != null ? ul.intValue() : 1;
            Timestamp createdAt = doc.getTimestamp("createdAt");
            String thumbnailUrl = doc.getString("thumbnailUrl");
            String targetRegionId = doc.getString("targetRegionId");

            list.add(new Post(
                    id,
                    title != null ? title : "",
                    urgency,
                    createdAt != null ? createdAt : Timestamp.now(),
                    thumbnailUrl != null ? thumbnailUrl : "",
                    targetRegionId != null ? targetRegionId : ""
            ));
        }
        posts.setValue(list);
    }
}
