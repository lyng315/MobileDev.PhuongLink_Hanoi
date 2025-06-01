package com.example.phuonglink_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeActivity: màn hình chính (Home) hiển thị danh sách bài đăng (“posts”)
 */
public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // 2. Thiết lập RecyclerView
        rvPosts = findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        rvPosts.setAdapter(postAdapter);

        // 3. Load posts từ Firestore
        loadPostsFromFirestore();
    }

    private void loadPostsFromFirestore() {
        // Lấy collection "posts", sắp xếp theo createdAt giảm dần
        db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("HomeActivity", "Lỗi khi lắng nghe posts", e);
                            Toast.makeText(HomeActivity.this, "Không thể tải bài đăng", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (snapshots != null) {
                            postList.clear();
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                if (doc.exists()) {
                                    String id = doc.getId();
                                    String title = doc.getString("title");
                                    Long ul = doc.getLong("urgencyLevel");
                                    int urgencyLevel = ul != null ? ul.intValue() : 1;
                                    Timestamp createdAt = doc.getTimestamp("createdAt");
                                    String thumbUrl = doc.getString("thumbnailUrl");
                                    if (thumbUrl == null) thumbUrl = "";

                                    postList.add(new Post(id, title, urgencyLevel, createdAt, thumbUrl));
                                }
                            }
                            postAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
