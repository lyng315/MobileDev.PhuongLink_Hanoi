package com.example.phuonglink_hanoi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class GuestActivity extends AppCompatActivity {

    private static final String PREFS_NAME      = "GuestPrefs";
    private static final String KEY_REGION_ID   = "regionId";
    private static final String KEY_REGION_NAME = "regionName";

    private MaterialButton      btnSignup;
    private MaterialButton      btnLogin;
    private AutoCompleteTextView actvRegion;
    private RecyclerView        rvPosts;

    private final FirebaseFirestore db           = FirebaseFirestore.getInstance();
    private final List<String>       regionNames = new ArrayList<>();
    private final List<String>       regionIds   = new ArrayList<>();

    private final List<Post> postList = new ArrayList<>();
    private PostAdapter      postAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        // Ánh xạ view
        btnSignup  = findViewById(R.id.btnGuestSignup);
        btnLogin   = findViewById(R.id.btnGuestLogin);
        actvRegion = findViewById(R.id.actvRegion);
        rvPosts    = findViewById(R.id.rvPosts);

        // Nút Đăng ký / Đăng nhập
        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        // Thiết lập RecyclerView
        postAdapter = new PostAdapter(this, postList, true);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setAdapter(postAdapter);
        postAdapter.setOnItemClickListener(post -> {
            Intent intent = new Intent(GuestActivity.this, GuestPostDetailsActivity.class);
            intent.putExtra(GuestPostDetailsActivity.EXTRA_POST_ID, post.getId());
            startActivity(intent);
        });

        // Load vùng và khởi tạo dropdown
        fetchRegions();
    }

    private void fetchRegions() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedId   = prefs.getString(KEY_REGION_ID, null);
        String savedName = prefs.getString(KEY_REGION_NAME, null);

        db.collection("regions")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    regionNames.clear();
                    regionIds.clear();

                    // Thêm tùy chọn mặc định
                    regionNames.add("Chưa chọn");
                    regionIds.add(null);

                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null) {
                            regionNames.add(name);
                            regionIds.add(doc.getId());
                        }
                    }

                    // Adapter cho dropdown
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_list_item_1,
                            regionNames
                    );
                    actvRegion.setAdapter(adapter);

                    // Hiển thị giá trị đã lưu (nếu có)
                    if (savedName != null) {
                        actvRegion.setText(savedName, false);
                    }

                    // Khi chọn lại vùng mới
                    actvRegion.setOnItemClickListener((parent, view, position, id) -> {
                        String regionId   = regionIds.get(position);
                        String regionName = regionNames.get(position);

                        prefs.edit()
                                .putString(KEY_REGION_ID, regionId)
                                .putString(KEY_REGION_NAME, regionName)
                                .apply();

                        loadPostsByRegion(regionId);
                    });

                    // Nếu đã có vùng lưu trước đó, tự động load bài viết
                    if (savedId != null) {
                        loadPostsByRegion(savedId);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không thể tải danh sách khu vực.", Toast.LENGTH_LONG).show()
                );
    }

    private void loadPostsByRegion(String regionId) {
        db.collection("posts")
                .whereEqualTo("targetRegionId", regionId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    postList.clear();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Post post = doc.toObject(Post.class);
                        if (post != null) {
                            post.setId(doc.getId());
                            postList.add(post);
                        }
                    }
                    postAdapter.updateList(postList);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không thể tải được bài viết.", Toast.LENGTH_SHORT).show()
                );
    }
}
