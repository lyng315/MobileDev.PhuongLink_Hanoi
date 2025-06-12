package com.example.phuonglink_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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
    private EditText etSearch;
    private FrameLayout contentContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        etSearch = findViewById(R.id.etSearch);
        contentContainer = findViewById(R.id.panel);
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

        // 4. Xử lý tìm kiếm theo tiêu đề
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    loadPostsFromFirestore(); // Hiện tất cả
                } else {
                    searchPostsByTitle(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //xử lý lọc
        MaterialButton btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FilterActivity.class);
            startActivityForResult(intent, 100);  // 100 là mã request
        });

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
    private void searchPostsByTitle(String keyword) {
        db.collection("posts")
                .orderBy("title")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("HomeActivity", "Lỗi tìm kiếm", e);
                        Toast.makeText(HomeActivity.this, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    postList.clear();
                    if (snapshots != null) {
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
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String category = data.getStringExtra("category");
            String levelStr = data.getStringExtra("level");

            // level trong FilterActivity là String, bạn nên convert nó sang int (nếu urgencyLevel là int)
            int urgencyLevel = mapLevelStringToInt(levelStr);

            filterPosts(category, urgencyLevel);
        }
    }
    private int mapLevelStringToInt(String levelStr) {
        switch (levelStr) {
            case "Khẩn cấp":
                return 3;
            case "Quan trọng":
                return 2;
            case "Bình thường":
                return 1;
            default:
                return 1;
        }
    }


    private void filterPosts(String category, int urgencyLevel) {
        db.collection("posts")
                .whereEqualTo("categoryId", category)
                .whereEqualTo("urgencyLevel", urgencyLevel)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("HomeActivity", "Lỗi khi lọc bài viết", e);
                        Toast.makeText(HomeActivity.this, "Không thể tải bài viết lọc", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    postList.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            if (doc.exists()) {
                                String id = doc.getId();
                                String title = doc.getString("title");
                                Long ul = doc.getLong("urgencyLevel");
                                int urgencyLevelPost = ul != null ? ul.intValue() : 1;
                                Timestamp createdAt = doc.getTimestamp("createdAt");
                                String thumbUrl = doc.getString("thumbnailUrl");
                                if (thumbUrl == null) thumbUrl = "";

                                postList.add(new Post(id, title, urgencyLevelPost, createdAt, thumbUrl));
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }


}
