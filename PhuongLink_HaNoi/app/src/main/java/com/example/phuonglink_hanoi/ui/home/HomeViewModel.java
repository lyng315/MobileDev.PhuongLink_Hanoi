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

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<List<Post>> getPosts() {
        return posts;
    }

    /** Chỉ load bài viết có targetRegionId = regionId của user */
    public void loadPostsByUserRegion() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String regionId = userDoc.getString("regionId");
                    if (regionId != null) {
                        db.collection("posts")
                                .whereEqualTo("targetRegionId", regionId)   // ← dùng đúng field
                                .orderBy("createdAt", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(this::mapSnapshotToPosts)
                                .addOnFailureListener(Throwable::printStackTrace);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void mapSnapshotToPosts(QuerySnapshot snapshots) {
        List<Post> list = new ArrayList<>();
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            String id              = doc.getId();
            String title           = doc.getString("title");
            Long ul                = doc.getLong("urgencyLevel");
            int urgency            = ul != null ? ul.intValue() : 1;
            Timestamp createdAt    = doc.getTimestamp("createdAt");
            String thumbnailUrl    = doc.getString("thumbnailUrl");
            String targetRegionId  = doc.getString("targetRegionId");  // ← map đúng
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
