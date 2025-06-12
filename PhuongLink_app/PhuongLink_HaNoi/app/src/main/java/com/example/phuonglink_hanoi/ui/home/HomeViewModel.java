package com.example.phuonglink_hanoi.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phuonglink_hanoi.Post;
import com.google.firebase.Timestamp;
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

    public void loadPosts() {
        db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(this::mapSnapshotToPosts);
    }

    public void searchPostsByTitle(String keyword) {
        db.collection("posts")
                .orderBy("title")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnSuccessListener(this::mapSnapshotToPosts);
    }

    public void loadFilteredPosts(String category, int urgencyLevel) {
        db.collection("posts")
                .whereEqualTo("categoryId", category)
                .whereEqualTo("urgencyLevel", urgencyLevel)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(this::mapSnapshotToPosts);
    }

    private void mapSnapshotToPosts(QuerySnapshot snapshots) {
        List<Post> list = new ArrayList<>();
        for (DocumentSnapshot doc : snapshots.getDocuments()) {
            String id = doc.getId();
            String title = doc.getString("title");
            Long ul = doc.getLong("urgencyLevel");
            int urgency = ul != null ? ul.intValue() : 1;
            Timestamp createdAt = doc.getTimestamp("createdAt");
            String thumbnailUrl = doc.getString("thumbnailUrl");
            list.add(new Post(id, title, urgency, createdAt, thumbnailUrl != null ? thumbnailUrl : ""));
        }
        posts.setValue(list);
    }
}
